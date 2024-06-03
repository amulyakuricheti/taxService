package com.nike.mptaxmanager.route.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.amazonaws.services.s3.AmazonS3;
import com.nike.mptaxmanager.utils.S3Util;

@Component
@Slf4j
public class OneSourceResponsePersistProcessor implements Processor {

    public static final String EMPTY_STRING = "";
    public static final String RESPONSE = "response";

    @Value("${com.nike.s3.mptaxmanager.audit.bucket}")
    private String mptaxmanagerBucket;

    @Value("${audit.path}")
    private String auditPath;

    @Autowired
    private AmazonS3 s3Client;

    @Override
    public void process(Exchange exchange) {

        String order = exchange.getIn().getBody(String.class);
        String orderNumber = exchange.getIn().getHeader("orderNumber", String.class);
        String requestTimeStamp = exchange.getIn().getHeader("requestTimeStamp", String.class);
        try {
            if (order != null) {
                s3Client.putObject(mptaxmanagerBucket, S3Util.fileNameCreator(orderNumber, requestTimeStamp, auditPath, RESPONSE), order);
            } else {
                s3Client.putObject(mptaxmanagerBucket, S3Util.fileNameCreator(orderNumber, requestTimeStamp, auditPath, RESPONSE), EMPTY_STRING);
            }

            log.info("onesource response updated successfully for orderNumber={}, requestTimeStamp={}", orderNumber, requestTimeStamp);
        } catch (Exception exception) {
            log.error("error while updating the data. orderNumber={}, requestTimeStamp={}", orderNumber, requestTimeStamp, exception);
        }
    }

}

