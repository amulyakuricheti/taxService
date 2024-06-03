package com.nike.invoiceshipmentwrkr.configuration;

import com.nike.invoiceshipmentwrkr.constants.OscarConstants;
import com.nike.invoiceshipmentwrkr.exception.OrderDetailException;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RouteConfigGetOrderDetailsRoute extends RouteBuilder {

    public static final String PAYMENTS_ORDER_DETAIL_ROUTE_ID = "payments-orderDetail-route";
    public static final String PAYMENTS_ORDER_DETAIL_ROUTE_NAME = "direct:" + PAYMENTS_ORDER_DETAIL_ROUTE_ID;


    @Value("${order_detail.vipName}")
    private String orderDetailVIPName;

    @Value("${order_detail.urlSuffix}")
    private String orderDetailUrlSuffix;

    @Value("${camel.redeliveryDelayMs}")
    private long redeliverDelay;

    @Value("${camel.maxRedeliveryCount}")
    private int maxRedeliveryCount;

    @Value("${camel.pg_put.connectionTimeOut:10000}")
    private long connectionTimeOut;

    @Value("${camel.pg_put.SocketTimeout:10000}")
    private long socketTimeout;

    @Value("${camel.hystrix.timout:10000}")
    private int hystrixTimeout;

    @Value("${hystrix.http.protocol:http4}")
    private String httpProtocol;

    @Value("${order.detail.oscar.scope.read}")
    private String scope;

    @Override
    public void configure() {

        from(PAYMENTS_ORDER_DETAIL_ROUTE_NAME)
                .routeId(PAYMENTS_ORDER_DETAIL_ROUTE_ID)
                .routeDescription("TO call orderDetail")
                .streamCaching()
                .errorHandler(noErrorHandler())
                .log("Entered OD Route to fetch the OrderDetail Call")
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json;utf-8"))// to remove header which are populated by previous service call
                .setHeader(OscarConstants.SCOPE, constant(scope))
                .setBody(constant("")) //empty body to get call
                .hystrix()
                .hystrixConfiguration()
                .executionTimeoutInMilliseconds(hystrixTimeout)
                .end()// end of configuration block
                .serviceCall().name(orderDetailVIPName)
                .expression()
                .simple(httpProtocol + ":${header.CamelServiceCallServiceHost}:${header.CamelServiceCallServicePort}"
                        + orderDetailUrlSuffix
                        + "${property.OrderNumber}"
                        + "?httpClient.SocketTimeout=" + socketTimeout
                        + "&httpClient.ConnectTimeout=" + connectionTimeOut
                        + "&httpClientConfigurer=#customJWTNoRetryConfigurer"
                        + "&connectionClose=true")
                .end()
                .endHystrix()// end hystrix block
                .onFallback()
                .log(LoggingLevel.ERROR, "in orderDetail Call fallback ")
                .process(exchange -> {
                    throw new OrderDetailException(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class));
                }).end()
                .convertBodyTo(String.class)
                .log(LoggingLevel.INFO, "orderDetail response received from orderDetail call is ${body}")
                .end();
    }
}
