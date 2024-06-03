package com.nike.invoiceshipmentwrkr.configuration;

import com.amazonaws.services.glue.model.EntityNotFoundException;
import com.amazonaws.services.mq.model.BadRequestException;
import com.nike.dynamodb.DynamoDBSchemaException;
import com.nike.invoiceshipmentwrkr.processor.EnrichPACResponseProcessor;
import com.nike.invoiceshipmentwrkr.processor.ScheduledTaxShipmentEventProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ScheduledTaxShipmentEventRoute {
    @Autowired
    private ScheduledTaxShipmentEventProcessor scheduledTaxShipmentEventProcessor;
    @Autowired
    private EnrichPACResponseProcessor enrichPACResponseProcessor;
    @Bean(name = "scheduledTaxShipment")
    public RouteBuilder routeBuilder() {

        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                errorHandler(defaultErrorHandler()
                        .maximumRedeliveries(3)
                        .redeliveryDelay(1000)
                        .asyncDelayedRedelivery()
                        .retryAttemptedLogLevel(LoggingLevel.WARN));

                onException(BadRequestException.class, EntityNotFoundException.class, IllegalArgumentException.class, IllegalStateException.class)
                        .handled(true)
                        .log(LoggingLevel.ERROR, " Service Error =${exception.stacktrace}")
                        .maximumRedeliveries(3)
                        .delay(4000)
                        .end()
                        .stop();

                onException(DynamoDBSchemaException.class)
                        .handled(true)
                        .maximumRedeliveries(3)
                        .delay(2000)
                        .log(LoggingLevel.ERROR, "Record could not be fetch or updated  in dynamo db due to exception=${exception.stacktrace}")
                        .end()
                        .stop();
                onException(Exception.class)
                        .handled(true)
                        .maximumRedeliveries(3)
                        .delay(2000)
                        .log(LoggingLevel.ERROR, "ScheduledTaxShipmentEventRoute failed=${exception.stacktrace}")
                        .end()
                        .stop();



                from("direct:scheduledTaxShipmentRoute")
                        .streamCaching()
                        .routeId("ScheduledTaxShipmentRouteId")
                        .routeDescription("Router to check if tax events arrived for shipmwnt event")
                        .log(LoggingLevel.INFO, "ScheduledTaxShipment Route started, body=${body}, headers=${headers}")
                        .setHeader("timezone", simple("${body}"))
                        .process(scheduledTaxShipmentEventProcessor)
                        .log(LoggingLevel.INFO, "Results fetched from dynamo DB and passed to iterator")
                        .end();

            }
        };
    }
}
