package com.nike.invoiceshipmentwrkr.configuration;

//import com.nike.camel.processor.DistributedTraceProcessor;
import com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants;
import com.nike.invoiceshipmentwrkr.model.pulse.PulseEventMessageModel;
import com.nike.invoiceshipmentwrkr.model.pulse.PulseSQSMessageModel;
import com.nike.invoiceshipmentwrkr.processor.CloudOrderInvoiceProcessor;
import com.nike.invoiceshipmentwrkr.processor.GetShippingChargeReference;
import com.nike.invoiceshipmentwrkr.processor.InvoiceCreationPayloadProcessor;
import com.nike.invoiceshipmentwrkr.processor.InvoiceShipmentEventMessageProcessor;
import com.nike.invoiceshipmentwrkr.processor.InvoiceShipmentWrkrSQSMessageProcessor;
import com.nike.invoiceshipmentwrkr.processor.NikeIdInvoiceCreationPayloadProcessor;
import com.nike.invoiceshipmentwrkr.processor.NikeIdTaxPaylodProcessor;
import com.nike.invoiceshipmentwrkr.processor.ResponseAggregation;
import com.nike.invoiceshipmentwrkr.processor.ServiceOrderDetailAggregation;
import com.nike.invoiceshipmentwrkr.processor.ShipNodeCacheProcessor;
import com.nike.invoiceshipmentwrkr.processor.ShipNodeDetailAggregation7;
import com.nike.invoiceshipmentwrkr.processor.ShipmentEventProcessor;
import com.nike.invoiceshipmentwrkr.processor.ShipmentProcessor;
import com.nike.invoiceshipmentwrkr.processor.StoreShippingChargeReference;
import com.nike.invoiceshipmentwrkr.processor.TaxDetailsAggregation;
import com.nike.invoiceshipmentwrkr.processor.TaxPayloadProcessor;
import com.nike.invoiceshipmentwrkr.service.HandleResponse;
import com.nike.invoiceshipmentwrkr.service.S3ConfigurationServiceShipment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.sqs.SqsConstants;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.IS_ELIGIBLE_FOR_PAC_POST;
import static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.IS_FLIKART_MYNTRA;
import static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.IS_NIKEJP;
import static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.LINE_TYPE_INLINE;
import  static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.IS_STORE_ORDER;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.builder.PredicateBuilder.and;
import static org.apache.camel.builder.PredicateBuilder.not;
import static org.apache.camel.builder.PredicateBuilder.or;

@Configuration
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class InvoiceShipmentWrkrRouteConfiguration {

    private static final String EVENT_MANAGER_HEADER = "X-Nike-AppName";
    public static final String DIRECT_PROCESS_FROM_SQS = "direct:ProcessFromSQS";

    @Value("${info.app.name}")
    private String appName;

    @Value("${sqs.invoiceshipment.wrkr.queue.name}")
    private String invoiceShipmentWrkrQueue;

    @Value("${sqs.max.no.messages}")
    private int maxNumberOfMessage = 1;

    @Value("${sqs.no.consumers}")
    private int numberOfConsumers = 1;

    @Value("${invoiceshipment.wrkr.maxRedeliveryCount}")
    private int retry;

    @Value("${invoiceshipment.wrkr.reDeliveryDelayMs}")
    private int delayMs;

    @Value("${camel.route.rest.to.vip.deliveryTimeOutMs:5000}")
    private int hystrixTimeout;

    @Value("${ordermgmt.pulse.vipName}")
    private String pulseVipName;

    @Value("${ordermgmt.pulse.urlSuffix}")
    private String pulseHost;

    @Value("${shipNode.vipName}")
    private String shipNodeVipName;

    @Value("${shipNode.hostName}")
    private String shipNodeHost;

    @Value("${tax.endPoint}")
    private String taxEndPoint;

    @Value("${tax.oscar.scope}")
    private String taxScope;

    @Value("${sqs.backOffMultiplier:2}")
    private int backOffMultiplier;

    @Value("${postToTax:false}")
    private boolean postToTax;

    @Autowired
    private ShipmentProcessor shipmentProcessor;

    @Autowired
    private ShipmentEventProcessor shipmentEventProcessor;

    @Autowired
    private ShipNodeDetailAggregation7  shipNodeDetailAggregation;

    @Autowired
    private InvoiceShipmentWrkrSQSMessageProcessor sqsMessageProcessor;

    @Autowired
    private InvoiceShipmentEventMessageProcessor eventMessageProcessor;

    @Autowired
    private TaxPayloadProcessor taxPayloadProcessor;

    @Autowired
    private TaxDetailsAggregation taxDetailsAggregation;

    @Autowired
    private ServiceOrderDetailAggregation orderDetailAggregation;

    @Autowired
    private InvoiceCreationPayloadProcessor invoiceCreationPayloadProcessor;

    @Autowired
    private NikeIdInvoiceCreationPayloadProcessor nikeIdInvoiceCreationPayloadProcessor;

    @Autowired
    private CloudOrderInvoiceProcessor cloudOrderInvoiceProcessor;

    @Autowired
    private ShipNodeCacheProcessor shipNodeCacheProcessor;

    @Autowired
    private NikeIdTaxPaylodProcessor nikeIdTaxPaylodProcessor;

    @Autowired
    private StoreShippingChargeReference storeShippingChargeReference;

    @Autowired
    private GetShippingChargeReference getShippingChargeReference;

    @Autowired
    private S3ConfigurationServiceShipment s3ConfigurationService;


    @Bean
    RouteBuilder raMessageConsumerRouteBuilder() {
        log.info("Invoice Shipment Worker Message consumer route builder");
        String fromUri =
                "aws-sqs://"
                        + invoiceShipmentWrkrQueue
                        + "?amazonSQSClient=#amazonSQSClient&concurrentConsumers="
                        + numberOfConsumers
                        + "&maxMessagesPerPoll="
                        + maxNumberOfMessage
                        + "&deleteAfterRead=true"
                        + "&deleteIfFiltered=true&messageAttributeNames=All";
        log.info("SQS fromUri" + fromUri);

        final RouteBuilder routeBuilder =
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        onException(Exception.class)
                                .maximumRedeliveries(retry)
                                .redeliveryDelay(delayMs)
                                .asyncDelayedRedelivery()
                                .retryAttemptedLogLevel(LoggingLevel.INFO)
                                .log("Exception occurred while processing the Inline request:${exception.stacktrace}")
                                .stop();

                        //Predicate isEmptyShipNode = exchangeProperty("shipNode").isNull();
                        Predicate callTaxEndpoint = constant(postToTax).isEqualTo(true);
                        Predicate shipNodeFoundInCache = exchangeProperty("shipNodeFoundInCache").isEqualTo(true);
                        Predicate inlineOrder = exchangeProperty(LINE_TYPE_INLINE).isEqualTo(true);
                        Predicate isNIKEJPOrder = exchangeProperty(IS_NIKEJP).isEqualTo(true);
                        Predicate isNotStoreJPOrder = exchangeProperty(IS_STORE_ORDER).isEqualTo(false);
                        Predicate isNotFlipkartMyntraFlow = exchangeProperty(IS_FLIKART_MYNTRA).isEqualTo(false);
                        Predicate isEligibleForPacPost = exchangeProperty(IS_ELIGIBLE_FOR_PAC_POST).isEqualTo(1);


                        Predicate inlineAllowed = constant(s3ConfigurationService.getFeatureFlag().getInline()).isEqualTo(true);
                        Predicate nikeIdAllowed = constant(s3ConfigurationService.getFeatureFlag().getNikeid()).isEqualTo(true);

                        Predicate inlinePostToSterling = and(inlineAllowed, inlineOrder);
                        Predicate nikeIdPostToSterling = and(nikeIdAllowed, not(inlineOrder));

                        Predicate postToSterling = or(inlinePostToSterling, nikeIdPostToSterling);


                        from("direct:apiProcessXML1")
                                .to("direct:processXML1")
                                .bean(HandleResponse.class, "buildSuccessResponse")
                                .end();

                        from(fromUri)
                                .streamCaching()
                                .to(DIRECT_PROCESS_FROM_SQS)
                                .end();

                        from(DIRECT_PROCESS_FROM_SQS)
//                                .bean(DistributedTraceProcessor.class)
                                .routeId("invoiceshipmentwrkr request")
                                .routeDescription("Consumer listen to invoiceshipmentwrkr SQS queue and process the message")
//                                .bean(DistributedTraceProcessor.class)
                                .log(LoggingLevel.INFO, "Message received=${body}")
                                .log(LoggingLevel.INFO, "Message successfully received from SQS queue for Inline Order")
                            //    .setProperty(SqsConstants.RECEIPT_HANDLE, header(SqsConstants.RECEIPT_HANDLE))
                                .unmarshal().json(JsonLibrary.Jackson, PulseSQSMessageModel.class)
                                .process(sqsMessageProcessor) // This would set UUID in the header
                                .log(LoggingLevel.INFO, "UUID received=${body} ")
                                .process(eventMessageProcessor)
                             //   .to("direct:GetPulseMessage")
                                .choice()
                                .when(isNotFlipkartMyntraFlow)
                                .to("direct:processXML1")
                                .log(LoggingLevel.INFO, "Successfully posted NikeInline CreateCloudInvoice to DOMS")
                                .process(storeShippingChargeReference)
                                .otherwise()
                                .log(LoggingLevel.INFO, "Triggering flipkart-myntra flow in the shipment event route")
                                .log(LoggingLevel.INFO, "Message from Event Management for flipkart myntra=${body}")
                                .process(shipmentEventProcessor)
                                .choice()
                                .when(isEligibleForPacPost).to("direct:enrichPACResponse").endChoice().endChoice();


                        from("direct:callOrderDetailForService")
                                .enrich(RouteConfigGetOrderDetailsRoute.PAYMENTS_ORDER_DETAIL_ROUTE_NAME, orderDetailAggregation)
                                .log(LoggingLevel.INFO, "Completed OrderDetail Mapping for NIKE JP=${body}")
                                .end();

                        from("direct:processXML1").setProperty(LINE_TYPE_INLINE, constant(true))
                                .process(shipmentProcessor) // This would set ShipNodeIds and OrderNumber in the header, shipping charge sent flag
                                .to("direct:callShipNode")
                                .process(getShippingChargeReference)
                                .choice()
                                .when(isNIKEJPOrder)
                                .log(LoggingLevel.INFO, "Invoke OrderDetail Call for OrderNumber=${exchangeProperty.OrderNumber}")
                                .to("direct:callOrderDetailForService")
                                .log("Processing Completed For NIKE JP Order with order Number =${exchangeProperty.OrderNumber}")
                                .otherwise()
                                .log("Skipping Order Details Calls since not a NIKE JP Order for order Number =${exchangeProperty.OrderNumber}")
                                .end()
                                .choice()
                                .when(isNotStoreJPOrder)
                                .to("direct:callTax")
                                .otherwise()
                                .setProperty("postToTax", constant(false))
                                .log("Skipping Tax calls for Store JP")
                                .to("direct:invoiceCreation")
                                .end();

                        from("direct:callShipNode")
                                .choice()
                                .when(shipNodeFoundInCache)
                                .log("ShipNode found in cache.")
                                .process(shipNodeCacheProcessor)
                                .otherwise()
                                .enrich("direct:GetShipNode", shipNodeDetailAggregation)
                                .end()
                                .end();

                        from("direct:callTax")
                                .choice()
                                .when(callTaxEndpoint)
                                .setProperty("postToTax", constant(postToTax))
                                .enrich("direct:PostToTax", taxDetailsAggregation)
                                .otherwise()
                                .setProperty("postToTax", constant(false))
                                .log("Tax call was not made as the feature flag is set to FALSE. All the tax values for Invoice Creation will be defaulted")
                                .end()
                                .to("direct:invoiceCreation");


                        from("direct:invoiceCreation")
                                .choice()
                                .when(inlineOrder)
                                .process(invoiceCreationPayloadProcessor)
                                .otherwise()
                                .process(nikeIdInvoiceCreationPayloadProcessor)
                                .end()
                                .process(cloudOrderInvoiceProcessor)
                                .choice()
                                .when(postToSterling)
                                .enrich("direct:PostToPAC", new ResponseAggregation())
                                .otherwise()
                                .log("not posting to sterling with ShipmentNo=${exchangeProperty.ShipmentNo}")
                                .end()
                                .end();

                        from("direct:GetPulseMessage")
                                .streamCaching()
//                                .bean(DistributedTraceProcessor.class)
                                .routeId("invoiceshipmentwrkr-GetPulseMessage")
                                .routeDescription("GET the Pulse Message from PULSE")
                                .log(LoggingLevel.INFO, "GET call to get the Pulse Message, headers=${headers}")
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
                                .log(LoggingLevel.INFO, "Message from EventManagement=${body}")
                                .log(LoggingLevel.INFO, "Message received from Event Management for ShipmentNo:${exchangeProperty.ShipmentNo}")
                                .unmarshal().json(JsonLibrary.Jackson, PulseEventMessageModel.class)
                                .process(eventMessageProcessor)
                                .end();

                        from("direct:GetShipNode")
                                .streamCaching()
//                                .bean(DistributedTraceProcessor.class)
                                .routeId("invoiceshipmentwrkr-GetShipNode")
                                .routeDescription("GET the shipNode details from shipNode service")
                                .log(LoggingLevel.INFO, "Invoke ShipNode GET to get shipNode address, headers=${headers}")
                                .convertBodyTo(String.class)
                                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                                .hystrix().hystrixConfiguration().executionTimeoutInMilliseconds(hystrixTimeout).end()
                                .removeHeaders("CamelServiceCall*")
                                .serviceCall().name(shipNodeVipName)
                                .expression()
                                .simple("http4:${header.CamelServiceCallServiceHost}:${header.CamelServiceCallServicePort}"
                                        + shipNodeHost
                                        + "?filter=ids(${exchangeProperty.shipNode})"
                                        + "&throwExceptionOnFailure=false"
                                        + "&httpClient.SocketTimeout=10000"
                                        + "&httpClient.ConnectTimeout=5000"
                                        + "&httpClientConfigurer=#customJWTNoRetryConfigurer"
                                        + "&connectionClose=true").end()
                                .endHystrix().end()
                                .log(LoggingLevel.INFO, "Response from ShipNode service, ShipmentNo=${exchangeProperty.ShipmentNo}, body=${body}")
                                .convertBodyTo(String.class)
                                .end();

                        from("direct:PostToTax")
                                .streamCaching()
//                                .bean(DistributedTraceProcessor.class)
                                .routeId("invoiceshipmentwrkr-PostToTax")
                                .routeDescription("POST to Tax Service to get the Tax Information")
                                .setHeader(InvoiceShipmentWrkrConstants.SCOPE, constant(taxScope))
                                .choice()
                                .when(inlineOrder)
                                .process(taxPayloadProcessor)
                                .otherwise()
                                .process(nikeIdTaxPaylodProcessor)
                                .end()
                                .marshal().json(JsonLibrary.Jackson, List.class)
                                .convertBodyTo(String.class)
                                .log(LoggingLevel.INFO, "POST to Tax Endpoint, headers=${headers}")
                                .log(LoggingLevel.INFO, "Invoke TAX Endpoint to get tax information for ShipmentNo=${exchangeProperty.ShipmentNo}, body=${body}")
                                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                                .hystrix().hystrixConfiguration().executionTimeoutInMilliseconds(hystrixTimeout).end()
                                .removeHeaders("CamelServiceCall*")
                                .to(taxEndPoint + "?throwExceptionOnFailure=true&httpClient.SocketTimeout=10000&httpClient.ConnectTimeout=2000&connectionClose=true&httpClientConfigurer=#customJWTNoRetryConfigurer")
                                .endHystrix().end()
                                .choice()
                                .when(header(HTTP_RESPONSE_CODE).isEqualTo("206"))
                                .log(LoggingLevel.INFO, "Response from Tax Endpoint is Partial, ShipmentNo=${exchangeProperty.ShipmentNo}, ResponseCode=${header.CamelHttpResponseCode}")
                                .otherwise()
                                .log(LoggingLevel.INFO, "Response from Tax Endpoint is Complete, ShipmentNo=${exchangeProperty.ShipmentNo}, ResponseCode=${header.CamelHttpResponseCode}")
                                .end()
                                .convertBodyTo(String.class)
                                .removeHeader(InvoiceShipmentWrkrConstants.SCOPE)
                                .end();
                    }
                };
        return routeBuilder;
    }
}
