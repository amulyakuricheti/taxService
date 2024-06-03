package com.nike.invoiceshipmentwrkr.configuration;

//import com.nike.camel.processor.DistributedTraceProcessor;
import com.nike.invoiceshipmentwrkr.model.taxevent.TaxDetailEvent;
import com.nike.invoiceshipmentwrkr.processor.TaxEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import static com.nike.invoiceshipmentwrkr.constants.InvoiceShipmentWrkrConstants.IS_ELIGIBLE_FOR_PAC_POST;

@Configuration
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ReprocessorRouteConfiguration extends RouteBuilder {

    @Autowired
    private TaxEventProcessor taxEventProcessor;

    @Value("${sqs.tax.nsp.retry.queue}")
    private String taxEventExceptionalProcessInputQueue;

    @Value("${sqs.wait.time.seconds}")
    private int waitTimeSeconds;


    @Override
    public void configure() throws Exception {

        Predicate isEligibleForPacPost = exchangeProperty(IS_ELIGIBLE_FOR_PAC_POST).isEqualTo(1);

         String sqsUri = new StringBuilder()
                .append("aws-sqs://")
                .append(taxEventExceptionalProcessInputQueue)
                .append("?amazonSQSClient=#amazonSQSClient")
                .append("&messageAttributeNames=All")
                .append("&deleteAfterRead=true")
                .append("&waitTimeSeconds=")
                .append(waitTimeSeconds).toString();

        from(sqsUri)
                .routeId("Reprocessing Route for processing tax events from DLQ")
                .description("This route takes reprocessed message from SQS for posting Cloud Invoice XML to DOMS")
                .streamCaching()
            //    .bean(DistributedTraceProcessor.class)
                .log(LoggingLevel.INFO, "Tax Event Consumed from SQS Queue = ${body}")
                .unmarshal().json(JsonLibrary.Jackson, TaxDetailEvent.class, true)
                .process(taxEventProcessor)
                .choice()
                .when(isEligibleForPacPost)
                .log("Reprocessed message from SQS is Eligible for AsyncBridge Posting")
                .to("direct:enrichPACResponse")
                .endChoice()
                .end();

    }



}
