package com.nike.mptaxmanager.service;

import com.nike.mptaxmanager.model.ErrorResponse;
import com.nike.mptaxmanager.model.onesource.response.ObjectFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HandleTaxManagerResponse {

    private static final String PW_REGEX = "<PASSWORD>.*<\\/PASSWORD>";
    private static final String PW_REPLACEMENT = "<PASSWORD>******</PASSWORD>";
    private static final String ERROR_CODE = "FailureInTaxManager";

    public ResponseEntity buildSuccessResponse(Exchange exchange) {

        log.info("Building success response");
        String responseString = exchange.getIn().getBody(String.class);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_XML).body(responseString);
    }

    public ResponseEntity buildErrorResponse(Exchange exchange) {
        log.info("Building error response!!");
        String orderType = exchange.getMessage().getHeader("OrderType", String.class);
        String response = exchange.getIn().getBody(String.class);
        if (StringUtils.isNotBlank(response)) {
            response = response.replaceAll(PW_REGEX, PW_REPLACEMENT);
        }
        if ("STORE".equalsIgnoreCase(orderType)) {
            log.info("Building 204 response as the Http Status for the Store order is other than 200");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).contentType(MediaType.APPLICATION_XML).body(response);
        }

        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        if (null != cause) {
            log.error("Error occurred in Tax Manager service, Message={}", cause.getMessage());
            return prepareErrorResponse(cause.getMessage());
        }
        return prepareErrorResponse("Unknown error occurred in Tax Manager service while processing the request.");
    }

    public ResponseEntity build204Response(Exchange exchange) {
        log.info("Building 204 response as the Http Status from Rules Engine is other than 200");
        String response = exchange.getIn().getBody(String.class);
        if (StringUtils.isNotBlank(response)) {
            response = response.replaceAll(PW_REGEX, PW_REPLACEMENT);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).contentType(MediaType.APPLICATION_XML).body(response);
    }

    public ResponseEntity prepareErrorResponse(final String errorDesc) {
        ObjectFactory objectFactory = new ObjectFactory();
        ErrorResponse errorResponse = objectFactory.createErrorResponse();
        errorResponse.setErrorCode(ERROR_CODE);
        errorResponse.setErrorDescription(errorDesc);
        errorResponse.setError("Y");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_XML).body(errorResponse);
    }

    public ResponseEntity prepareTCCErrorResponse(final String errorDesc) {
        ObjectFactory objectFactory = new ObjectFactory();
        ErrorResponse errorResponse = objectFactory.createErrorResponse();
        errorResponse.setErrorCode(ERROR_CODE);
        errorResponse.setErrorDescription(errorDesc);
        errorResponse.setError("Y");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_XML).body(errorResponse);
    }


}

