package com.nike.invoiceshipmentwrkr.configuration;

//import com.nike.camel.processor.DistributedTraceProcessor;
import com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants;
import com.nike.invoiceshipmentwrkr.model.pulse.BopisPulseSQSMessageModel;
import com.nike.invoiceshipmentwrkr.processor.ResponseAggregation;

import com.nike.invoiceshipmentwrkr.processor.bopis.BopisCloudOrderInvoiceProcessor;
import com.nike.invoiceshipmentwrkr.processor.bopis.GetBopisShippingChargeReference;
import com.nike.invoiceshipmentwrkr.processor.bopis.BopisInvoiceCreationPayloadProcessor;
import com.nike.invoiceshipmentwrkr.processor.bopis.BopisInvoiceShipmentEventMessageProcessor;
import com.nike.invoiceshipmentwrkr.processor.bopis.BopisOrderDetailAggregation;
import com.nike.invoiceshipmentwrkr.processor.bopis.BopisShipNodeCacheProcessor;
import com.nike.invoiceshipmentwrkr.processor.bopis.BopisShipNodeDetailAggregation;
import com.nike.invoiceshipmentwrkr.processor.bopis.BopisTaxDetailsAggregation;
import com.nike.invoiceshipmentwrkr.processor.bopis.InvoiceShipmentWrkrBopisSQSMessageProcessor;
import com.nike.invoiceshipmentwrkr.processor.bopis.TaxPayloadBopisProcessor;
import com.nike.invoiceshipmentwrkr.service.S3ConfigurationServiceShipment;
import com.nike.invoiceshipmentwrkr.utils.InvoiceShipmentWrkrUtils;
//import com.nike.mp.titan.camel.orderdetail.routebuilder.CallOrderDetailsV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.sqs.SqsConstants;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.LINE_TYPE_INLINE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.builder.PredicateBuilder.and;

@Configuration
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class InvoiceShipmentWrkrNikeBopisConfiguration {

    private static final String EVENT_MANAGER_HEADER = "X-Nike-AppName";
    public static final String DIRECT_PROCESS_FROM_BOPIS_SQS = "direct:NikeBopisProcessFromSQS";

    @Value("${sqs.invoiceshipment.wrkr.bopis.queue.name}")
    private String invoiceShipmentWrkrNikeBopisQueue;

    @Value("${sqs.max.no.messages}")
    private int maxNumberOfMessage;

    @Value("${sqs.no.consumers}")
    private int numberOfConsumers;

    @Value("${invoiceshipment.wrkr.maxRedeliveryCount}")
    private int retry;

    @Value("${invoiceshipment.wrkr.reDeliveryDelayMs}")
    private int delayMs;

    @Value("${info.app.name}")
    private String appName;

    @Value("${camel.route.rest.to.vip.deliveryTimeOutMs:5000}")
    private int hystrixTimeout;

    @Value("${tax.oscar.scope}")
    private String taxScope;

    @Value("${ordermgmt.pulse.vipName}")
    private String pulseVipName;
    @Value("${shipNode.vipName}")
    private String shipNodeVipName;
    @Value("${shipNode.hostName}")
    private String shipNodeHost;
    @Value("${postToTax:false}")
    private boolean postToTax;
    @Value("${tax.endPoint}")
    private String taxEndPoint;
    @Value("${invoice.pac.publisher.host}")
    private String pacEndpoint;

    @Autowired
    private BopisInvoiceShipmentEventMessageProcessor eventMessageProcessor;

    @Autowired
    private InvoiceShipmentWrkrBopisSQSMessageProcessor bopisSqsMessageProcessor;

    @Autowired
    @Qualifier("OrderDetailProcessor")
    private Processor orderDetailsProcessor;

//    @Autowired
//    private CallOrderDetailsV2 callOrderDetailsV2;

    @Autowired
    private BopisOrderDetailAggregation orderDetailAggregation;

    @Autowired
    private GetBopisShippingChargeReference getBopisShippingChargeReference;
    @Autowired
    private BopisTaxDetailsAggregation bopisTaxDetailsAggregation;
    @Autowired
    private BopisInvoiceCreationPayloadProcessor bopisInvoiceCreationPayloadProcessor;

    @Autowired
    private BopisCloudOrderInvoiceProcessor bopisCloudOrderInvoiceProcessor;

    @Autowired
    private S3ConfigurationServiceShipment s3ConfigurationService;

    @Autowired
    private TaxPayloadBopisProcessor taxPayloadBopisProcessor;

    @Autowired
    private BopisShipNodeCacheProcessor bopisShipNodeCacheProcessor;
    @Autowired
    private BopisShipNodeDetailAggregation bopisShipNodeDetailAggregation;


    @Bean
    RouteBuilder sqsNikeBopisMessageProcessingRouteBuilder() {
        log.info("Invoice Shipment Worker Bopis Message consumer route builder");
        String fromBopisUri = "aws-sqs://"
                + invoiceShipmentWrkrNikeBopisQueue
                + "?amazonSQSClient=#amazonSQSClient&concurrentConsumers="
                + numberOfConsumers
                + "&maxMessagesPerPoll="
                + maxNumberOfMessage
                + "&deleteAfterRead=true"
                + "&deleteIfFiltered=true&messageAttributeNames=All";

        return new RouteBuilder() {

            public void configure() throws Exception {

                onException(Exception.class)
                        .maximumRedeliveries(retry)
                        .redeliveryDelay(delayMs)
                        .asyncDelayedRedelivery()
                        .retryAttemptedLogLevel(LoggingLevel.INFO)
                        .log("Exception occurred while processing the NikeBopis request:${exception.stacktrace}")
                        .stop();


//                includeRoutes(callOrderDetailsV2.callOrderDetailsV2(CallOrderDetailsV2.Options.builder()
//                        .throwExceptionOnFailure(false)
//                        .orderNumberHeaderName("OrderNo")
//                        .internalLogging(true).build()));

                Predicate callTaxEndpoint = constant(postToTax).isEqualTo(true);
                Predicate inlineOrder = exchangeProperty(LINE_TYPE_INLINE).isEqualTo(true);
                Predicate inlineAllowed = constant(s3ConfigurationService.getFeatureFlag().getInline()).isEqualTo(true);
                Predicate postToSterling = and(inlineAllowed, inlineOrder);
                Predicate shipNodeFoundInCache = exchangeProperty("shipNodeFoundInCache").isEqualTo(true);

                from(fromBopisUri)
                        .streamCaching()
                        .to(DIRECT_PROCESS_FROM_BOPIS_SQS)
                        .end();

                from(DIRECT_PROCESS_FROM_BOPIS_SQS)
                        .routeId("InvoiceShipmentWrkrBopisSQSMessageProcessor")
                        .setProperty(SqsConstants.RECEIPT_HANDLE, header(SqsConstants.RECEIPT_HANDLE))
                        .routeDescription("NIKE BOPIS Tax calculation and Create Invoice processing")
//                        .bean(DistributedTraceProcessor.class)
                        .process(exchange -> exchange.setProperty(InvoiceShipmentWrkrUtils.START_TIME, System.currentTimeMillis()))
                        .log(LoggingLevel.INFO, "Message received from topic ce_vom_picked_up_event :" + fromBopisUri + " is : ${body}")
                        .unmarshal().json(JsonLibrary.Jackson, BopisPulseSQSMessageModel.class)
                        .process(bopisSqsMessageProcessor)
                        .setHeader(SqsConstants.RECEIPT_HANDLE, exchangeProperty(SqsConstants.RECEIPT_HANDLE))
                        .log(LoggingLevel.INFO, "UUID received=${body} ")
                        .to("direct:GetNikeBopisPulseMessage")
                        .to("direct:processNikeBopisXML");


                from("direct:processNikeBopisXML")
                        .setProperty(LINE_TYPE_INLINE, constant(true))
                        .log(LoggingLevel.INFO, "inside processNikeBopisXML")
                        .to("direct:ProcessNikeBopisPulseMessage")
                        .end();

                from("direct:GetNikeBopisPulseMessage")
                        .streamCaching()
//                        .bean(DistributedTraceProcessor.class)
                        .routeId("InvoiceShipmentWrkrBopisSQSMessageProcessor-GetPulseMessage")
                        .routeDescription("GET the Bopis Pulse Message from PULSE")
                        .log(LoggingLevel.INFO, "GET call to get the Bopis Pulse Message, headers=${headers}")
                        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                        .setHeader(EVENT_MANAGER_HEADER, constant(appName))
                        .setHeader(HttpHeaders.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
                        .hystrix().hystrixConfiguration().executionTimeoutInMilliseconds(hystrixTimeout).end()
                        .serviceCall().name(pulseVipName)
                        .expression()
                        .simple("http4:${header.CamelServiceCallServiceHost}:${header.CamelServiceCallServicePort}"
                                + "/order_mgmt/internal_events/v1/"
                                + "${body}"
                                + "?httpClient.SocketTimeout=10000"
                                + "&httpClient.ConnectTimeout=2000"
                                + "&httpClientConfigurer=#customJWTNoRetryConfigurer"
                                + "&connectionClose=true").end()
                        .endHystrix().end()
                        .log(LoggingLevel.INFO, "Bopis message from Event Management=${body}")
                        .process(eventMessageProcessor)
                        .end();

                from("direct:ProcessNikeBopisPulseMessage")
                        .setProperty(LINE_TYPE_INLINE, constant(true))
                        .log(LoggingLevel.INFO, "Invoke OrderDetail Call for OrderNumber=${exchangeProperty.OrderNo}")
                        .process(orderDetailsProcessor)
                     //   .enrich(CallOrderDetailsV2.GET_ORDERDETAILS_V2_ROUTE, orderDetailAggregation)
                        .log(LoggingLevel.INFO, "ProcessNikeBopisPulseMessage::enriched=${body}")
                        .to("direct:callShipNodeBopis")
                        .log(LoggingLevel.INFO, "ProcessNikeBopisPulseMessage::ShipNodeProcessed=${body}")
                        .process(getBopisShippingChargeReference)
                        .to("direct:callBopisGTax")
                        .end();

                from("direct:callShipNodeBopis")
                        .choice()
                        .when(shipNodeFoundInCache)
                        .log("Nike Bopis ShipNode found in cache.")
                        .process(bopisShipNodeCacheProcessor)
                        .otherwise()
                        .enrich("direct:GetShipNode", bopisShipNodeDetailAggregation)
                        .log(LoggingLevel.INFO, "callShipNodeBopis::shipNodeDetailAggregationBopis=${body}")
                        .end()
                        .end();

                from("direct:callBopisGTax")
                        .log(LoggingLevel.INFO, "inside callBopisGTax")
                        .choice()
                        .when(callTaxEndpoint)
                        .setProperty("postToTax", constant(postToTax))
                        .enrich("direct:PostToBopisGTax", bopisTaxDetailsAggregation)
                        .otherwise()
                        .setProperty("postToTax", constant(false))
                        .log("Tax call was not made as the feature flag is set to FALSE. All the tax values for Invoice Creation will be defaulted")
                        .end()
                        .process(bopisInvoiceCreationPayloadProcessor)
                        .process(bopisCloudOrderInvoiceProcessor)
                        .choice()
                        .when(postToSterling)
                        .enrich("direct:PostToPAC", new ResponseAggregation())
                        .otherwise()
                        .log("not posting to sterling with inlineOrder=${exchangeProperty(" + LINE_TYPE_INLINE + ")}")
                        .end()
                        .end();

                from("direct:PostToBopisGTax")
                        .log(LoggingLevel.INFO, "inside PostToBopisGTax")
                        .streamCaching()
//                        .bean(DistributedTraceProcessor.class)
                        .routeId("invoiceshipmentwrkr-PostToBopisGTax")
                        .routeDescription("POST to Tax Service to get the Tax Information")
                        .setHeader(InvoiceShipmentWrkrConstants.SCOPE, constant(taxScope))
                        .process(taxPayloadBopisProcessor)
                        .marshal().json(JsonLibrary.Jackson, List.class)
                        .convertBodyTo(String.class)
                        .log(LoggingLevel.INFO, "POST to Tax Endpoint, headers=${headers}")
                        .log(LoggingLevel.INFO, "Invoke TAX Endpoint to get tax information for shipAdviceNumber=${exchangeProperty.shipAdviceNumber}, body=${body}")
                        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                        .hystrix().hystrixConfiguration().executionTimeoutInMilliseconds(hystrixTimeout).end()
                        .removeHeaders("CamelServiceCall*")
                        .to(taxEndPoint + "?throwExceptionOnFailure=true&httpClient.SocketTimeout=10000&httpClient.ConnectTimeout=2000&connectionClose=true&httpClientConfigurer=#customJWTNoRetryConfigurer")
                        .endHystrix().end()
                        .choice()
                        .when(header(HTTP_RESPONSE_CODE).isEqualTo("206"))
                        .log(LoggingLevel.INFO, "Response from Tax Endpoint is Partial, shipAdviceNumber=${exchangeProperty.shipAdviceNumber}, ResponseCode=${header.CamelHttpResponseCode}")
                        .otherwise()
                        .log(LoggingLevel.INFO, "Response from Tax Endpoint is Complete, shipAdviceNumber=${exchangeProperty.shipAdviceNumber}, ResponseCode=${header.CamelHttpResponseCode}")
                        .end()
                        .convertBodyTo(String.class)
                        .end();
            }

        };
    }
}
