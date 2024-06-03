package com.nike.invoiceshipmentwrkr.configuration;


//import com.nike.camel.processor.DistributedTraceProcessor;
import com.nike.invoiceshipmentwrkr.model.taxevent.TaxDetailEvent;
import com.nike.invoiceshipmentwrkr.processor.TaxEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.apache.camel.LoggingLevel;
import org.apache.commons.lang3.exception.ExceptionUtils;

import static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.IS_ELIGIBLE_FOR_PAC_POST;


@Configuration
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RouteGetTaxEventConfiguration extends RouteBuilder {


    public static final String NSP_GET_TAX_EVENT_ROUTE = "direct:get-tax-event-from-nsp-route-id";

    @Value("${sqs.tax.nsp.retry.dlq}")
    private String taxEventExceptionalProcessInputDlq;


    @Autowired
    private TaxEventProcessor taxEventProcessor;

    @Value("${sqs.no.consumers:1}")
    private String numberOfConsumers;

    @Value("${sqs.max.no.messages:10}")
    private int maxNumberOfMessages;

    @Value("${sqs.wait.time.seconds}")
    private int waitTimeSeconds;



    @Override
    public void configure() throws Exception {

                String dlqUri = new StringBuilder()
                .append("aws-sqs://")
                .append(taxEventExceptionalProcessInputDlq)
                .append("?amazonSQSClient=#amazonSQSClient")
                .append("&messageAttributeNames=All")
                .append("&deleteAfterRead=true")
                .append("&waitTimeSeconds=")
                .append(waitTimeSeconds).toString();

        Predicate isEligibleForPacPost = exchangeProperty(IS_ELIGIBLE_FOR_PAC_POST).isEqualTo(1);

        onException(Exception.class)
                .log(LoggingLevel.INFO, "### Send Tax Event to SQS DLQ for further reprocessing: ${body}")
                .useOriginalMessage()
                .process(exchange -> {
                    log.error("Exception StackTrace for NSP Consumer Route = " + ExceptionUtils.getStackTrace((Throwable) exchange.getProperty(Exchange.EXCEPTION_CAUGHT)));
                })
                .to(dlqUri)
                .log(LoggingLevel.INFO, "Tax Event successfully posted to DLQ")
                .end();


        from(NSP_GET_TAX_EVENT_ROUTE)
                .routeId(NSP_GET_TAX_EVENT_ROUTE)
                .description("This route consumes Flipkart/Myntra tax events through NSP topic")
                .streamCaching()
//                .bean(DistributedTraceProcessor.class)
                .log(LoggingLevel.INFO, "Tax Event Received from NSP = ${body}")
                .unmarshal().json(JsonLibrary.Jackson, TaxDetailEvent.class, true)
                .process(taxEventProcessor)
                    .choice()
                        .when(isEligibleForPacPost)
                        .log("Eligible for AsyncBridge Posting")
                            .to("direct:enrichPACResponse")
                    .endChoice()
                .end();

    }
}
