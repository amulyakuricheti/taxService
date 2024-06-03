package com.nike.invoiceshipmentwrkr.configuration;

//import com.nike.camel.processor.DistributedTraceProcessor;
import com.nike.invoiceshipmentwrkr.model.pulse.PulseSQSMessageModel;
import com.nike.invoiceshipmentwrkr.processor.GetShippingChargeReference;
import com.nike.invoiceshipmentwrkr.processor.InvoiceShipmentWrkrSQSMessageProcessor;
import com.nike.invoiceshipmentwrkr.processor.NikeIdorderStatusChangeProcessor;
import com.nike.invoiceshipmentwrkr.processor.OrderDetailAggregation;
import com.nike.invoiceshipmentwrkr.processor.StoreShippingChargeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.sqs.SqsConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.LINE_TYPE_INLINE;
import static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.IS_NIKEID_STORE_ORDER;

@Configuration
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class InvoiceShipmentWrkrNikeIDConfiguration {

    public static final String DIRECT_PROCESS_FROM_NIKEID_SQS = "direct:NikeIdProcessFromSQS";

    @Value("${sqs.invoiceshipment.wrkr.nikeid.queue.name}")
    private String invoiceShipmentWrkrNikeIdQueue;

    @Value("${sqs.max.no.messages}")
    private int maxNumberOfMessage = 1;

    @Value("${sqs.no.consumers}")
    private int numberOfConsumers = 1;

    @Value("${invoiceshipment.wrkr.maxRedeliveryCount}")
    private int retry;

    @Value("${invoiceshipment.wrkr.reDeliveryDelayMs}")
    private int delayMs;

    @Autowired
    private NikeIdorderStatusChangeProcessor nikeIdorderStatusChangeProcessor;

    @Autowired
    private InvoiceShipmentWrkrSQSMessageProcessor sqsMessageProcessor;

    @Autowired
    private OrderDetailAggregation orderDetailAggregation;

    @Autowired
    private StoreShippingChargeReference storeShippingChargeReference;

    @Autowired
    private GetShippingChargeReference getShippingChargeReference;

    @Bean
    RouteBuilder invoiceShipmentNikeIdMessageConsumerRouteBuilder() {
        log.info("Invoice Shipment Worker Message consumer route builder");
        String nikeIdFromUri =
                "aws-sqs://"
                        + invoiceShipmentWrkrNikeIdQueue
                        + "?amazonSQSClient=#amazonSQSClient&concurrentConsumers="
                        + numberOfConsumers
                        + "&maxMessagesPerPoll="
                        + maxNumberOfMessage
                        + "&deleteAfterRead=true"
                        + "&deleteIfFiltered=true&messageAttributeNames=All";
        log.info("SQS nikeIdFromUri" + nikeIdFromUri);

        final RouteBuilder routeBuilder =
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        onException(Exception.class)
                                .maximumRedeliveries(retry)
                                .redeliveryDelay(delayMs)
                                .asyncDelayedRedelivery()
                                .retryAttemptedLogLevel(LoggingLevel.INFO)
                                .log("Exception occurred while processing the NikeId request:${exception.stacktrace}")
                                .stop();
                        Predicate isNotNikeIdStoreJPOrder = exchangeProperty(IS_NIKEID_STORE_ORDER).isEqualTo(false);

                        from(nikeIdFromUri)
                                .streamCaching()
                                .to(DIRECT_PROCESS_FROM_NIKEID_SQS)
                                .process(storeShippingChargeReference)
                                .end();

                        from(DIRECT_PROCESS_FROM_NIKEID_SQS)
//                                .bean(DistributedTraceProcessor.class)
                                .routeId("invoiceshipmentwrkr NikeId request")
                                .routeDescription("Consumer listen to invoiceshipmentwrkr SQS queue for NikeId and process the message")
//                                .bean(DistributedTraceProcessor.class)
                                .log(LoggingLevel.DEBUG, "Message received=${body}")
                                .log(LoggingLevel.INFO, "Message successfully received from SQS queue for NikeId Order")
                                .setProperty(SqsConstants.RECEIPT_HANDLE, header(SqsConstants.RECEIPT_HANDLE))
                                .unmarshal().json(JsonLibrary.Jackson, PulseSQSMessageModel.class)
                                .process(sqsMessageProcessor) // This would set UUID in the header
                                .log(LoggingLevel.INFO, "UUID received=${body} ")
                                .to("direct:GetPulseMessage")
                                .to("direct:processNikeIdXML")
                                .end();

                        from("direct:processNikeIdXML")
                                .setProperty(LINE_TYPE_INLINE, constant(false))
                                .process(nikeIdorderStatusChangeProcessor)
                                .to("direct:callOrderDetail")
                                .to("direct:callShipNode")
                                .process(getShippingChargeReference)
                                .choice()
                                .when(isNotNikeIdStoreJPOrder)
                                .log(LoggingLevel.INFO, "Proceeding for Tax for NikeID Orders")
                                .to("direct:callTax")
                                .log(LoggingLevel.INFO, "Successfully posted NikeID CreateCloudInvoice to DOMS")
                                .otherwise()
                                .log(LoggingLevel.INFO, "Skipping Tax Call for NIKEID JP Store Orders")
                                .setProperty("postToTax", constant(false))
                                .to("direct:invoiceCreation")
                                .end();

                        from("direct:callOrderDetail")
                                .enrich(RouteConfigGetOrderDetailsRoute.PAYMENTS_ORDER_DETAIL_ROUTE_NAME, orderDetailAggregation)
                                .log(LoggingLevel.INFO, "Completed OrderDetail Mapping")
                                .end();
                    }
                };
        return routeBuilder;
    }
}
