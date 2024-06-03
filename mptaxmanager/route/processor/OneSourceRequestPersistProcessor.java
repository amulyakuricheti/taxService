package com.nike.mptaxmanager.route.processor;

import com.amazonaws.services.s3.AmazonS3;
import com.nike.mptaxmanager.utils.S3Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class OneSourceRequestPersistProcessor implements Processor {

    public static final String PW_REGEX = "<PASSWORD>.*<\\/PASSWORD>";
    public static final String PW_REPLACEMENT = "<PASSWORD>******</PASSWORD>";
    public static final String EMPTY_STRING = "";
    public static final String REQUEST = "request";

    @Value("${com.nike.s3.mptaxmanager.audit.bucket}")
    private String mptaxmanagerBucket;

    @Value("${audit.path}")
    private String auditPath;

    @Autowired
    private AmazonS3 s3Client;

    @Override
    public void process(Exchange exchange) {

        String indata = exchange.getIn().getBody(String.class);
        String orderNumber = exchange.getIn().getHeader("orderNumber", String.class);
        String requestTimeStamp = exchange.getIn().getHeader("requestTimeStamp", String.class);
        try {
            if (indata != null) {
                indata = indata.replaceAll(PW_REGEX, PW_REPLACEMENT);
                s3Client.putObject(mptaxmanagerBucket, S3Util.fileNameCreator(orderNumber, requestTimeStamp, auditPath, REQUEST), indata);
            }  else {
            s3Client.putObject(mptaxmanagerBucket, S3Util.fileNameCreator(orderNumber, requestTimeStamp, auditPath, REQUEST), EMPTY_STRING);
                }
            log.info("Request persisted successfully for orderNumber={}, requestTimeStamp={}", orderNumber, requestTimeStamp);
        } catch (Exception exception) {
            log.error("error while saving the data. orderNumber={}, requestTimeStamp={}", orderNumber, requestTimeStamp, exception);
        }
    }

}

