//package com.nike.invoiceshipmentwrkr.configuration;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.nike.phylon.s3.LocalS3Client;
//import com.amazonaws.util.EC2MetadataUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.File;
//
//@Configuration
//@Slf4j
//@ConditionalOnProperty(value = "invoiceshipmentwrkr.enableS3Client", matchIfMissing = true)
//public class S3ConfigurationShipment {
//    @Value("${com.nike.s3.local:false}")
//    private boolean useLocalS3;
//    @Value("${com.nike.s3.local.path:#{null}}")
//    private String localS3Path;
//    @Value("${com.nike.s3.invoiceshipmentwrkr.configuration.bucket}")
//    private String inspectReturnsWrkrBucket;
//
//    @Bean(name = "amazonS3ClientShipment")
//    public AmazonS3 amazonS3ClientShipment() {
//        AmazonS3 client;
//        if (useLocalS3) {
//            client = new LocalS3Client(localS3Path);
//            client.createBucket(inspectReturnsWrkrBucket);
//            client.putObject(inspectReturnsWrkrBucket, "test.json", new File("data/s3/test.json"));
//            log.info("S3 is created in local at {}", localS3Path);
//        } else {
//            client = AmazonS3ClientBuilder
//                    .standard()
//                    .withRegion("us-east-1")
//                    .build();
//            log.info("AmazonS3Client is created");
//        }
//        return client;
//    }
//}
