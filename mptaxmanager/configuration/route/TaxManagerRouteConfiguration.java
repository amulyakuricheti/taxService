package com.nike.mptaxmanager.configuration.route;

//import com.nike.camel.processor.DistributedTraceProcessor;
import com.nike.camel.routebuilder.OutgoingRESTCallRouteConfiguration;
import com.nike.mptaxmanager.exception.InvalidPIIResponseException;
import com.nike.mptaxmanager.model.rulesengine.RulesEngineRequest;
import com.nike.mptaxmanager.processor.HeadersConfigurer;
import com.nike.mptaxmanager.processor.ShipNodeProcessor;
import com.nike.mptaxmanager.processor.ShipNodeDetailAggregation;
import com.nike.mptaxmanager.processor.TaxClassificationCodeAggregation;
import com.nike.mptaxmanager.processor.TaxClassificationCodeMappingProcessor;
import com.nike.mptaxmanager.processor.EnhanceTMPayloadProcessor;
import com.nike.mptaxmanager.processor.StandardOrderTMPayloadProcessor;
import com.nike.mptaxmanager.processor.CreateRulesEngineAggregrator;
import com.nike.mptaxmanager.processor.CreateRulesEngineRequest;
import com.nike.mptaxmanager.processor.PiiAggregator;
import com.nike.mptaxmanager.processor.SetTCCMarketPlaceFilter;
import com.nike.mptaxmanager.processor.TaxClassificationCodeCollectionStrategy;
import com.nike.mptaxmanager.processor.ValidateAndDefaultExtnRef;
import com.nike.mptaxmanager.route.processor.OneSourceRequestPersistProcessor;
import com.nike.mptaxmanager.route.processor.OneSourceResponsePersistProcessor;
import com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants;
import com.nike.mptaxmanager.route.processor.PrepareOneSourceAuthProcessor;
import com.nike.mptaxmanager.route.processor.StoreProcessor;
import com.nike.mptaxmanager.service.HandleTaxManagerResponse;
import com.nike.phylon.BlueprintConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;


import static org.apache.camel.builder.PredicateBuilder.or;

/**
 * A Camel route in Spring Boot.
 * <p>
 * Notice that we use @Component on the class to make the route automatic
 * discovered by Spring Boot
 * <p>
 * This is a sample route the aim is to take a message, process it via processor
 * and the post to configured http endpoint. (default to system.out)
 * <p>
 * The HTTP endpoint to where the payload should go is specified from property
 * file
 */

@Configuration
public class TaxManagerRouteConfiguration {

    @Autowired
    private BlueprintConfiguration values;

    public static final String ROUTE_SUFFIX = "mainRoute";

    private static final String POST_TO_ONESOURCE = "direct:postToOneSourceRoute";

    @Value("${camel.route.Rest.to.http.maxRedeliveryCount:5}")
    private int maxRedeliveryCount;

    @Value("${camel.route.Rest.to.http.redeliveryDelayMs:100}")
    private long redeliveryDelayMs;

    @Value("${camel.route.rest.to.vip.deliveryTimeOutMs:5000}")
    private int hystrixTimeout;

    @Value("${pii.vipName}")
    private String piiVipName;

    @Value("${pii.hostName}")
    private String piiHost;

    @Value("${shipNode.vipName}")
    private String shipNodeVipName;

    @Value("${shipNode.hostName}")
    private String shipNodeHost;

    @Value("${rulesEngine.vipName}")
    private String rulesEngineVipName;

    @Value("${rulesEngine.hostName}")
    private String rulesEngineHost;

    @Value("${oneSource.vipName}")
    private String oneSourceVipName;

    @Value("${oneSource.hostName}")
    private String oneSourceHost;

    @Value("${tcc.apiGatewayURL}")
    private String tccEndpoint;

    @Value("${tcc.vipName}")
    private String tccVipName;

    @Value("${tcc.enabled}")
    private boolean tccEnabled;

    @Value("${oscar.service-scopes.rule_tax_recalculation.POST}")
    private String ruleEngineScope;

    @Autowired
    private ValidateAndDefaultExtnRef validateAndDefaultExtnRef;


    @Autowired
    private HeadersConfigurer headersConfigurer;

    @Autowired
    private ShipNodeProcessor shipNodeProcessor;

    @Autowired
    private OneSourceResponsePersistProcessor oneSourceResponsePersistProcessor;

    @Autowired
    private OneSourceRequestPersistProcessor oneSourceRequestPersistProcessor;

    @Autowired
    private PrepareOneSourceAuthProcessor prepareOneSourceAuthProcessor;

    @Autowired
    private ShipNodeDetailAggregation shipNodeDetailAggregation;

    @Autowired
    private OutgoingRESTCallRouteConfiguration routeConfiguration;

    @Autowired
    private TaxClassificationCodeAggregation taxClassificationCodeAggregation;

    @Autowired
    private TaxClassificationCodeCollectionStrategy taxClassificationCodeCollectionStrategy;

    @Autowired
    private TaxClassificationCodeMappingProcessor taxClassificationCodeMappingProcessor;

    @Bean(name = "routeBuilder")
    public RouteBuilder route() {

        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {

                handleDownstreamExceptions(this, maxRedeliveryCount, redeliveryDelayMs);

                handleLocalExceptions(this, maxRedeliveryCount, redeliveryDelayMs);

                final RouteBuilder oneSourceRoute = routeConfiguration.outgoingRESTRouteBuilder(oneSourceVipName,
                        oneSourceHost, POST_TO_ONESOURCE, "postToRoute");

                includeRoutes(oneSourceRoute);
                Predicate isRecalcTaxRequired = header("taxRecalcRequired").isEqualTo(true);
                Predicate addressId = header("piiAddressIdSet").isNotNull();
                Predicate isEmptyShipNodeIdsStr = header("shipNodeIds").isNull();
                Predicate isLineExist = exchangeProperty("isLineExist");
                Predicate isStoreOrder = or(header("OrderType").isEqualTo("STORE"),
                        header("Channel").isEqualTo("nike.store.fpos"), header("Channel").isEqualTo("nike.store.mpos"));
                Predicate isUSOrder = header("EnterpriseCode").isEqualTo("NIKEUS");
                Predicate isUSOrEuropeOrder = or(header("EnterpriseCode").isEqualTo("NIKEUS"), header("EnterpriseCode").isEqualTo("NIKEEUROPE"));
                Predicate callTcc = constant(tccEnabled).isEqualTo(false);
                Predicate upcCheck = header("tccUPCCodeList").isNotNull();
                from("direct:RestInput")
                        .streamCaching()
                        .routeId(values.getAppName() + ":" + ROUTE_SUFFIX)
                        .description("Route to itemProxy request")
//                        .bean(DistributedTraceProcessor.class)
                        .log(LoggingLevel.INFO, "Received Calculate tax request, body=${body} and headers=${headers}")
                        .setHeader("isStoreOrder", constant(isStoreOrder))
                        .process(validateAndDefaultExtnRef)
                        .process(headersConfigurer)
                        .enrich("direct:PostToRulesEngine", new CreateRulesEngineAggregrator())
                        .choice()
                        .when(isRecalcTaxRequired)
                                .log(LoggingLevel.INFO, "Recalculating taxes, headers=${headers}")
                                .to("direct:GetPII")
                                .enrich("direct:GetShipNodes", shipNodeDetailAggregation)
                                 .log(LoggingLevel.INFO, "headers value before TCC call = ${headers}")
                                 .choice()
                                    .when(isUSOrEuropeOrder)
                                        .choice()
                                        .when(upcCheck)
                                        .choice()
                                        .when(callTcc)
                                            .log(LoggingLevel.INFO, "Not invoking TCC service, headers=${headers}")
                                        .otherwise()
                                            .log(LoggingLevel.INFO, "Invoking TCC service, headers=${headers}, body=${body}")
                                            .to("direct:GetTaxClassificationCode")
                                            .bean(taxClassificationCodeCollectionStrategy)
                                        .endChoice()
                                        .otherwise()
                                         .log(LoggingLevel.INFO, "TCC UPCCode set is null, not invoking TCC call. Headers=${headers}")
                                    .endChoice()
                                   .otherwise()
                                         .log(LoggingLevel.INFO, "No TCC Service call for orderNumber=${header.orderNumber} and EnterpriseCode=${header.EnterpriseCode}")
                        .endChoice()
                        .log(LoggingLevel.INFO, "Setting enhanced attributes to payload, headers=${headers}")
                        .process(new EnhanceTMPayloadProcessor())
                        .log(LoggingLevel.INFO, "Added enhanced attributes to payload for orderNumber=${header.orderNumber}, body=${body}")
                        .process(exchange -> {
                            String orderNumber = exchange.getIn().getHeader("orderNumber", String.class);
                            String body = exchange.getIn().getBody(String.class);
                            log.info("New Added enhanced attributes to payload for orderNumber={}, body={}", orderNumber, body);
                        })
                        .choice()
                            .when(isUSOrder)
                                .log(LoggingLevel.INFO, "Executing TaxRecalculation flow for US, headers=${headers}")
                                .to("direct:RecalculateUSTaxes")
                             .otherwise()
                                .log(LoggingLevel.INFO, "Executing TaxRecalculation flow for Europe, headers=${headers}")
                                .to("direct:RecalculateEuropeTaxes")
                             .endChoice()
                            .to("direct:PostToOneSource")
                            .otherwise()
                                .log("Tax recalculation not required, headers=${headers}, and RulesEngine response=${property.CamelHttpResponseCode}")
                                .bean(HandleTaxManagerResponse.class, "build204Response")
                        .endChoice()
                        .end();

                from("direct:GetTaxClassificationCode")
                        .streamCaching()
                        .routeId("mptaxmanager-TCCCall")
                        .routeDescription("Invokes GET TaxClassificationCode to get the tax classification code for upcCodes.")
                        .split(header("tccUPCCodeList"), taxClassificationCodeAggregation).stopOnException()
                        .setHeader("tccUPCCodeSet", simple("${body}"))
                        .log(LoggingLevel.INFO, "Invoke TCCService to get additional information, headers=${headers}")
                        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                        .setHeader("serviceName", constant("TCC"))
                        .setBody(constant(null))
                        .toD(tccEndpoint
                                + "?filter=gtins(${header.tccUPCCodeSet})"
                                + "&marketplace=${header.countryCode}"
                                + "&throwExceptionOnFailure=true"
                                + "&httpClient.SocketTimeout=10000"
                                + "&httpClient.ConnectTimeout=5000"
                                + "&httpClientConfigurer=#customJWTNoRetryConfigurer"
                                + "&connectionClose=true").end()
                        .log(LoggingLevel.INFO, "TCC response, headers=${headers} and body=${body}")
                        .convertBodyTo(String.class)
                        .log(LoggingLevel.INFO, "Converted body: ${body}")
                        .end();

                from("direct:RecalculateUSTaxes")
                        .streamCaching()
                        .routeId("mptaxmanager-RecalculateUSTaxes")
                        .routeDescription("Invokes RecalculateUSTaxes to execute TaxRecalculation flow for US.")
                        .log(LoggingLevel.INFO, "Recalculate tax for US orderNumber=${header.orderNumber} and body=${body}")
                        .choice()
                            .when(isStoreOrder)
                                .log(LoggingLevel.INFO, "Invoking US STORE order flow, headers=${headers}")
                                .process(new StoreProcessor())
                                .enrich("xslt:/xslt/US/US_Store-OneSourceRequest.xsl")
                                .log(LoggingLevel.INFO, "Successfully created the OneSource request for US store, headers=${headers} and body=${body}")
                            .otherwise()
                                .log(LoggingLevel.INFO, "Invoking US STANDARD order flow, headers=${headers}")
                                .enrich("xslt:/xslt/US/US_StandardOrder-TaxCalculations.xsl")
                                .log(LoggingLevel.INFO, "Successfully created standardOrder US payload, body=${body}")
                                .process(new StandardOrderTMPayloadProcessor())
                                .enrich("xslt:/xslt/US/US_TaxManager-To-OneSource.xsl")
                                .log(LoggingLevel.INFO, "Successfully created the OneSource request for US STANDARD orderNumber=${header.orderNumber}, body=${body}")
                        .endChoice();

                from("direct:RecalculateEuropeTaxes")
                        .streamCaching()
                        .routeId("mptaxmanager-RecalculateEuropeTaxes")
                        .routeDescription("Invokes RecalculateEuropeTaxes to execute TaxRecalculation flow for Europe.")
                        .log(LoggingLevel.INFO, "Recalculate tax for Europe orderNumber=${header.orderNumber} and body=${body}")
                        .choice()
                            .when(isStoreOrder)
                                .log(LoggingLevel.INFO, "Invoking EU STORE order flow, headers=${headers}")
                                .process(new StoreProcessor())
                                .enrich("xslt:/xslt/OneSourceRequestForStore.xsl")
                                .log(LoggingLevel.INFO, "Successfully created the OneSource request for EU STORE, headers=${headers} and body=${body}")
                            .otherwise()
                                .log(LoggingLevel.INFO, "Invoking EU STANDARD order flow, headers=${headers}")
                                .enrich("xslt:/xslt/StandardOrder-TaxCalculations.xsl")
                                .log(LoggingLevel.INFO, "Successfully created standardOrder EU payload, body=${body}")
                                .process(new StandardOrderTMPayloadProcessor())
                                .enrich("xslt:/xslt/TaxManager-To-OneSource.xsl")
                                .log(LoggingLevel.INFO, "Successfully created the OneSource request for EU STANDARD, headers=${headers}, body=${body}")
                        .endChoice();

                from("direct:PostToOneSource")
                        .streamCaching()
                        .routeId("mptaxmanager-OneSource")
                        .routeDescription("Post to OneSource to get the tax details")
                        .log(LoggingLevel.INFO, "Framed request to OneSource for orderNumber=${header.orderNumber} and requestBody=${body}")
                        .process(prepareOneSourceAuthProcessor)
                        .choice()
                             .when(isLineExist)
                                 .log(LoggingLevel.INFO, "Persisting OneSource request, headers=${headers}")
                                 .process(oneSourceRequestPersistProcessor)
                                 .log(LoggingLevel.INFO, "Invoke OneSource to get the tax details for the orderNumber=${header.orderNumber}")
                                 .to(POST_TO_ONESOURCE)
                                 .log(LoggingLevel.INFO, "Response from OneSource for orderNumber=${header.orderNumber} and body=${body}")
                                 .process(oneSourceResponsePersistProcessor)
                                 .log(LoggingLevel.INFO, "Successfully processed the OneSource request, headers=${headers}")
                                 .choice()
                                     .when(isUSOrEuropeOrder)
                                            .log(LoggingLevel.INFO, "Executing TCC mapping in MPTaxmanager response for EU Orders")
                                            .process(taxClassificationCodeMappingProcessor)
                                     .otherwise()
                                            .log(LoggingLevel.INFO, "TCC mapping in MPTaxmanager response for non Europe orders in not required")
                                 .endChoice()
                                 .enrich("xslt:/xslt/OneSource-To-Sterling.xsl")
                                 .bean(HandleTaxManagerResponse.class, "buildSuccessResponse")
                             .otherwise()
                                 .log(LoggingLevel.INFO, "Not invoking OneSource, headers=${headers}")
                                 .bean(HandleTaxManagerResponse.class, "build204Response")
                        .endChoice()
                        .end();

                from("direct:GetPII")
                        .streamCaching()
                        .routeId("mptaxmanager-GetPII")
                        .routeDescription("Invokes GET PiiService to get the additional information for the order line.")
                        .log(LoggingLevel.INFO, "Get address details, headers=${headers}")
                        .choice()
                            .when(addressId)
                                .log(LoggingLevel.INFO, "Invoking PII service, headers=${headers}")
                                .enrich("direct:GetPIIInfo", new PiiAggregator())
                                .log(LoggingLevel.INFO, "Successfully retrieved PII address info, headers=${headers}")
                            .otherwise()
                                .process(new SetTCCMarketPlaceFilter())
                                .log(LoggingLevel.INFO, "Not invoking PII service as addressIDSet is NULL, headers=${headers}")
                        .endChoice()
                        .end();

                from("direct:GetPIIInfo")
                        .streamCaching()
                        .routeId("mptaxmanager-PIICall")
                        .routeDescription("Invokes GET PiiService to get the additional information for the order line.")
                        .log(LoggingLevel.INFO, "Invoke PiiService to get additional information, headers=${headers}")
                        .convertBodyTo(String.class)
                        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    //    .hystrix().hystrixConfiguration().executionTimeoutInMilliseconds(hystrixTimeout).end()
                    //    .setBody(constant("{\"objects\":[{\"id\":\"2503738e-acb1-42d8-ba73-6b52d061831e\",\"address\":{\"address1\":\"1180 N Brightleaf Blvd\",\"address2\":\"\",\"address3\":\"\",\"address4\":\"\",\"city\":\"Smithfield\",\"latitude\":\"35.5184783935547\",\"longitude\":\"-78.3165512084961\",\"country\":\"US\",\"state\":\"NC\",\"zipCode\":\"27577\"},\"recipient\":{\"firstName\":\"Tyiesha\",\"lastName\":\"Stone\",\"alternateLastName\":\"Stone\"},\"contactInformation\":{\"dayPhoneNumber\":\"9196317756\",\"email\":\"tyiesha.stone@johnstonnc.com\",\"phoneNumber\":{\"countryCode\":\"1\",\"subscriberNumber\":\"9196317756\"}},\"orderNumber\":\"C01398490953\",\"addressType\":\"SHIP_TO\",\"orderLineNumbers\":[1],\"resourceType\":\"order_mgmt/addresses\",\"links\":{\"self\":{\"ref\":\"order_mgmt/addresses/v1/2503738e-acb1-42d8-ba73-6b52d061831e\"}}}]}")) // Static JSON response
                     //   .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200)) // Set HTTP response code
                      //  .serviceCall().name(piiVipName)

                        .toD("https4://pii-dev.consumer-om-test.nikecloud.com/order_mgmt/addresses/v1"
                            + "?filter=id(${header.piiAddressIdSet})"
                            + "?throwExceptionOnFailure=true"
                            + "&httpClient.SocketTimeout=10000"
                            + "&httpClient.ConnectTimeout=5000"
                            + "&httpClientConfigurer=#customJWTNoRetryConfigurer"
                            + "&connectionClose=true").end()

                        .log(LoggingLevel.INFO, "PII response, headers=${headers} and body=${body}")
                        .convertBodyTo(String.class)
                        .end();

                from("direct:GetShipNodes")
                        .streamCaching()
                        .routeId("mptaxmanager-GetShipNodes")
                        .routeDescription("GET the shipNode details from shipNode service")
                        .log(LoggingLevel.INFO, "GET the shipNode details, headers=${headers}")
                        .process(shipNodeProcessor)
                        .choice()
                            .when(isEmptyShipNodeIdsStr)
                                .setBody(constant(null))
                                .log(LoggingLevel.INFO, "NO shipNodeService call made, headers=${headers}")
                            .otherwise()
                                .log(LoggingLevel.INFO, "Invoke ShipNode GET to get shipNode address, headers=${headers}")
                                .setBody(constant("{\"shipNodes\":[{\"id\":\"78\",\"uuid\":\"00E02648-B54C-4FE2-BF8B-97ABDB1FF3B6\",\"type\":\"RETAIL\",\"address\":{\"name\":\"Nike Factory Store - Myrtle Beach\",\"department\":\"Nike\",\"address1\":\"Tanger Outlet Center - Myrtle Beach\",\"address2\":\"4642 Factory Stores Blvd #FF100\",\"address3\":\"\",\"address4\":\"\",\"address5\":\"\",\"address6\":\"\",\"city\":\"Myrtle Beach\",\"state\":\"SC\",\"zipCode\":\"29579-0963\",\"country\":\"US\",\"shortZipCode\":\"\",\"lattitude\":\"33.752314\",\"longitude\":\"-78.960667\"},\"email\":{\"primary\":\"DoNotReply_Nike_Factory_Store__Myrtle_Beach@nike.com\",\"alternate\":\"\"},\"phone\":{\"day\":\"18439030110\",\"evening\":\"\",\"mobile\":\"\"},\"resourceType\":null,\"links\":null}],\"resourceType\":\"ship/nodedetails/v2\",\"links\":{\"self\":{\"ref\":\"ship/nodedetails/v2/?filter=ids(78)\"}}}")) // Static JSON response
                                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200)) // Set HTTP response code
                                .convertBodyTo(String.class)
                                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_UTF8_VALUE))
                                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                          //      .hystrix().hystrixConfiguration().executionTimeoutInMilliseconds(hystrixTimeout).end()
                          //      .removeHeaders("CamelServiceCall*")
                          //      .serviceCall().name(shipNodeVipName)
//                                .expression()
//                                .toD("https4://api-test.nikecloud.com/ship/node_details/v3/(${header.shipNodeIds})"
//                                        + "&throwExceptionOnFailure=false"
//                                        + "&httpClient.SocketTimeout=10000"
//                                        + "&httpClient.ConnectTimeout=5000"
//                                        + "&httpClientConfigurer=#customJWTNoRetryConfigurer"
//                                        + "&connectionClose=true").end()
                                .log(LoggingLevel.INFO, "Response from ShipNode service, orderNumber=${header.orderNumber}, body=${body}")
                                .convertBodyTo(String.class)
                        .endChoice()
                        .end();

                from("direct:PostToRulesEngine")
                        .streamCaching()
                        .routeId("mptaxmanager-RulesEngineCall")
                        .routeDescription("Invokes Rules Engine to get the tax related rules information.")
                        .log(LoggingLevel.INFO, "Invoke RulesEngine to get additional information, headers=${headers}")
                        .bean(CreateRulesEngineRequest.class)
                        .log(LoggingLevel.INFO, "Created Request for Rules Engine, headers=${headers}")
                        .marshal().json(JsonLibrary.Jackson, RulesEngineRequest.class)
                        .log(LoggingLevel.INFO, "Marshalling successful in PostToRulesEngine-RulesEngineRequest route Object to JSON, headers=${headers}")
                        .convertBodyTo(String.class)
                        .log(LoggingLevel.INFO, "rulesEngineRequest=${body}")
                        .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .toD("https4://rule-engine-dev.consumer-om-test.nikecloud.com/order_mgmt/rule_tax_recalculation/v1"
                                + "?throwExceptionOnFailure=true"
                                + "&httpClientConfigurer=#customJWTNoRetryConfigurer"
                                + "&connectionClose=true").end()
                        .log(LoggingLevel.INFO, "RulesEngine response, headers=${headers} and body=${body}")
                        .convertBodyTo(String.class)
                        .end();
            }
        };
    }

    private static void handleDownstreamExceptions(RouteBuilder routeBuilder, int maxRedeliveryCount, long redeliveryDelayMs) {
        routeBuilder.onException(Exception.class)
                .log(LoggingLevel.INFO, "Got an exception while calling TCC, headers=${headers} and exception=${exception.stacktrace}")
                .maximumRedeliveries(maxRedeliveryCount)
                .redeliveryDelay(redeliveryDelayMs)
                .logExhaustedMessageHistory(true)
                .retryAttemptedLogLevel(LoggingLevel.INFO)
                .retriesExhaustedLogLevel(LoggingLevel.ERROR)
                .onWhen(exchange -> {
                    String serviceName = exchange.getIn().getHeader("serviceName", String.class);
                    exchange.getIn().removeHeader("serviceName");
                    return "TCC".equalsIgnoreCase(serviceName);
                })
                .log(LoggingLevel.INFO, "ReTried 5 times and hence not triggering TCE call for orderNo=${headers.orderNumber} and tccUPCCodeList = ${headers.tccUPCCodeSet}")
                .bean(HandleTaxManagerResponse.class, "prepareTCCErrorResponse")
                .logStackTrace(true)
                .handled(true);

        routeBuilder.onException(InvalidPIIResponseException.class)
                .logExhaustedMessageHistory(true)
                .log(LoggingLevel.INFO, "Exceptions occurred while aggregating downstream service responses PII routes, headers=${headers}")
                .bean(HandleTaxManagerResponse.class, "buildErrorResponse")
                .logStackTrace(true)
                .handled(true);

    }

    private static void handleLocalExceptions(RouteBuilder routeBuilder, int maxRedeliveryCount, long redeliveryDelayMs) {
        routeBuilder.onException(Exception.class)
                .maximumRedeliveries(maxRedeliveryCount)
                .redeliveryDelay(redeliveryDelayMs)
                .logExhaustedMessageHistory(true)
                .retryAttemptedLogLevel(LoggingLevel.INFO)
                .retriesExhaustedLogLevel(LoggingLevel.ERROR)
                .log(LoggingLevel.ERROR, "Exceptions occurred while processing request, headers=${headers}, exception=${exception.stacktrace}")
                .bean(HandleTaxManagerResponse.class, "buildErrorResponse")
                .logStackTrace(true)
                .handled(true);
    }
}

