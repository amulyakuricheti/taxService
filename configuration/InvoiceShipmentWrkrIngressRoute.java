/*
package com.nike.invoiceshipmentwrkr.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nike.camel.processor.DistributedTraceProcessor;
import com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants;
import com.nike.invoiceshipmentwrkr.model.pulse.PulseEventMessageModel;
import com.nike.invoiceshipmentwrkr.model.pulse.PulseSQSMessageModel;
import com.nike.invoiceshipmentwrkr.processor.*;
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

import static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.*;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.builder.PredicateBuilder.*;

@Configuration
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class InvoiceShipmentWrkrIngressRoute {
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

    @Value("${shipNode.vipName}")
    private String shipNodeVipName;

    @Value("${shipNode.hostName}")
    private String shipNodeHost;

    @Value("${tax.endPoint}")
    private String taxEndPoint;

    @Value("${tax.oscar.scope}")
    private String taxScope;

    @Value("${postToTax:false}")
    private boolean postToTax;

    @Autowired
    private ShipmentProcessor shipmentProcessor;

    @Autowired
    private ShipNodeDetailAggregation7 shipNodeDetailAggregation;

    @Autowired
    private InvoiceShipmentEventMessageProcessor eventMessageProcessor;

    @Autowired
    private TaxPayloadProcessor taxPayloadProcessor;

    @Autowired
    private TaxDetailsAggregation taxDetailsAggregation;

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
    private GetShippingChargeReference getShippingChargeReference;

    @Autowired
    private S3ConfigurationServiceShipment s3ConfigurationService;


    @Bean
    RouteBuilder raMessageConsumerRouteBuilderIngress() {
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

                        Predicate inlineAllowed = constant(s3ConfigurationService.getFeatureFlag().getInline()).isEqualTo(true);
                        Predicate nikeIdAllowed = constant(s3ConfigurationService.getFeatureFlag().getNikeid()).isEqualTo(true);

                        Predicate inlinePostToSterling = and(inlineAllowed, inlineOrder);
                        Predicate nikeIdPostToSterling = and(nikeIdAllowed, not(inlineOrder));

                        Predicate postToSterling = or(inlinePostToSterling, nikeIdPostToSterling);


                        from("direct:apiProcessXML")
                                .to("direct:processXML")
                                .bean(HandleResponse.class, "buildSuccessResponse")
                                .end();

                        from(fromUri)
                                .streamCaching()
                                .to(DIRECT_PROCESS_FROM_SQS)
                                .end();

                        from(DIRECT_PROCESS_FROM_SQS)
                                .bean(DistributedTraceProcessor.class)
                                .routeId("invoiceshipmentwrkr request")
                                .routeDescription("Consumer listen to invoiceshipmentwrkr SQS queue and process the message")
                                .bean(DistributedTraceProcessor.class)
                                .log(LoggingLevel.INFO, "Message received=${body}")
                                .process(exchange -> {
                                    ObjectMapper mapper = new ObjectMapper();
                                    JsonNode nspEvent = mapper.readTree(exchange.getIn().getBody(String.class));
                                    JsonNode orderInvoice = nspEvent.get(NOTIFICATION);
                                    exchange.getIn().setBody(orderInvoice, String.class);
                                })

                                .process(shipmentProcessor) // This would set ShipNodeIds and OrderNumber in the header, shipping charge sent flag
                                .to("direct:callShipNode")
                                .process(getShippingChargeReference)
                                .to("direct:callTax")
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

                        from("direct:GetShipNode")
                                .streamCaching()
                                .bean(DistributedTraceProcessor.class)
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
                                .bean(DistributedTraceProcessor.class)
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
*/
