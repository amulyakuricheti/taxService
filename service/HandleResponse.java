package com.nike.invoiceshipmentwrkr.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Configuration
@Slf4j
public class HandleResponse {
    public ResponseEntity buildSuccessResponse(Exchange exchange) {
        log.info("Building Final success response ... ");
        //String responseString = exchange.getIn().getBody(String.class);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_XML).body("Successfully processed !!!");

    }
}
