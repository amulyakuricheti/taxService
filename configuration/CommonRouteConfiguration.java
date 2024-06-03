package com.nike.invoiceshipmentwrkr.configuration;

//import com.nike.camel.processor.DistributedTraceProcessor;
import com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants;
import com.nike.invoiceshipmentwrkr.processor.AsyncBridgeResponseProcessor;
import com.nike.invoiceshipmentwrkr.processor.FRDetailsAggregation;
import com.nike.invoiceshipmentwrkr.processor.FRDetailsProcessor;
import com.nike.invoiceshipmentwrkr.processor.NSPPostInvoiceEventProcessor;
import com.nike.invoiceshipmentwrkr.processor.ResponseAggregation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class CommonRouteConfiguration extends RouteBuilder {

    @Autowired
    private FRDetailsAggregation frDetailAggregation;

    @Autowired
    private FRDetailsProcessor frDetailsProcessor;

    @Autowired
    private AsyncBridgeResponseProcessor asyncBridgeResponseProcessor;

    @Autowired
    private NSPPostInvoiceEventProcessor nspPostInvoiceEventProcessor;

    @Value("${fulfilment.vipName}")
    private String fulfilmentVipName;

    @Value("${fulfilment.hostName}")
    private String fulfilmentNodeHost;

    @Value("${fulfilment.oscar.scope}")
    private String fulfilmentScope;

    @Value("${camel.route.rest.to.vip.deliveryTimeOutMs:5000}")
    private int hystrixTimeout;

    @Value("${invoice.pac.publisher.host}")
    private String pacEndpoint;

    @Override
    public void configure() throws Exception {
        from("direct:enrichPACResponse")
                .streamCaching()
//                .bean(DistributedTraceProcessor.class)
                .threads(10, 20)
                .process(frDetailsProcessor) // This would set OrderNumber in the header for FR call
                .enrich("direct:GetFRDetails", frDetailAggregation)
                .log(LoggingLevel.INFO, "Completed FR Call")
                .enrich("direct:PostToPAC", new ResponseAggregation())
                .process(asyncBridgeResponseProcessor)
                .log(LoggingLevel.INFO, "Successfully posted CloudShipmentInvoice XML to AsyncBridge, OrderNumber=${exchangeProperty.OrderNumber}")
                .end();

        from("direct:GetFRDetails")
                .streamCaching()
//                .bean(DistributedTraceProcessor.class)
                .routeId("invoiceshipmentwrkr-GetFRDetails")
                .routeDescription("GET the Fulfilment details from FR service")
                .log(LoggingLevel.INFO, "Invoke FR service to get the Fulfilment details, headers=${headers}")
                .convertBodyTo(String.class)
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .setHeader(InvoiceShipmentWrkrConstants.SCOPE, constant(fulfilmentScope))
                .hystrix().hystrixConfiguration().executionTimeoutInMilliseconds(hystrixTimeout).end()
                .removeHeaders("CamelServiceCall*")
                .serviceCall().name(fulfilmentVipName)
                .expression()
                .simple("http4:${header.CamelServiceCallServiceHost}:${header.CamelServiceCallServicePort}"
                        + fulfilmentNodeHost
                        + "?filter=fulfillmentRequestNumber(${exchangeProperty.PONumber})"
                        + "&throwExceptionOnFailure=false"
                        + "&httpClient.SocketTimeout=10000"
                        + "&httpClient.ConnectTimeout=5000"
                        + "&httpClientConfigurer=#customJWTNoRetryConfigurer"
                        + "&connectionClose=true").end()
                .endHystrix().end()
                .log(LoggingLevel.INFO, "Response from Fullfillment service, PurchaseOrderNumber=${exchangeProperty.PONumber}, body=${body}")
                .convertBodyTo(String.class)
                .removeHeader(InvoiceShipmentWrkrConstants.SCOPE)
                .end();

        from("direct:PostToPAC")
                .streamCaching()
//                .bean(DistributedTraceProcessor.class)
                .routeId("PostToPAC")
                .routeDescription("Route for posting CloudInvoiceShipment to PAC")
                .log(LoggingLevel.INFO, "Posting ShipmentInvoice to PAC, body=${body}")
                .convertBodyTo(String.class, "UTF-8")
                .log(LoggingLevel.INFO, "Posting CloudShipmentInvoice XML to AsyncBridge, OrderNumber=${exchangeProperty.OrderNumber}, body=${body}")
                .process(nspPostInvoiceEventProcessor)
                .log(LoggingLevel.INFO, "Successfully posted to PAC for OrderNumber=${exchangeProperty.OrderNumber}")
                .end();
    }
}
