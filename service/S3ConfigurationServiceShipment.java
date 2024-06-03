package com.nike.invoiceshipmentwrkr.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nike.invoiceshipmentwrkr.model.S3ConfigurationModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class S3ConfigurationServiceShipment {

    @Autowired
    private AmazonS3 s3Client;

    @Value("${com.nike.s3.invoiceshipmentwrkr.configuration.key:applications/order_mgmt/titan/invoiceshipmentwrkr/configuration/configuration.json}")
    private String key;
    private Date lastModificationTime = null;

    @Value("#{${envBucketMap:{prod: 'nike-commerce-prod-app-internal', test: 'nike-commerce-test-app-internal'}}}")
    private Map<String, String> envBucketMap;

    @Value("${com.nike.s3.wmslite.configuration.bucket.name:#{null}}")
    private String bucketName;

    private S3ConfigurationModel.FeatureFlag featureFlag;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public S3ConfigurationServiceShipment(@Value("${com.nike.s3.invoiceshipmentwrkr.configuration.bucket}") String bucketName, @Value("${com.nike.s3.invoiceshipmentwrkr.configuration.key}") String key) {
        this.bucketName = bucketName;
        this.key = key;
    }

    public S3ConfigurationModel.FeatureFlag getFeatureFlag() {
        if (null == featureFlag) {
            loadCache();
        }
        return featureFlag;
    }

    public void loadCache() {
        if (bucketName == null && envBucketMap != null) {
            bucketName = envBucketMap.get(System.getenv("CLOUD_ENVIRONMENT"));
            if (bucketName == null) {
                bucketName = envBucketMap.get("test");
            }
            log.info("Bucket Name was null and set to {} with CLOUD_ENVIRONMENT={} and envBucketMap={}", bucketName,
                    System.getenv("CLOUD_ENVIRONMENT"), envBucketMap);
        } else {
            log.info("Bucket Name already set to {} and envBucketMap={}", bucketName, envBucketMap);
        }
        ObjectMetadata metadata = s3Client.getObjectMetadata(bucketName, key);
        if (lastModificationTime != null && lastModificationTime.compareTo(metadata.getLastModified()) == 0) {
            log.info("There is no change for the S3 Object Bucket={} Key={} lastModifiedDate={}", bucketName, key, lastModificationTime);
            return;
        }
        log.info("Loading config from Bucket={} Key={} lastModifiedDate={}", bucketName, key, lastModificationTime);
        loadS3ConfigurationData();

        //comment out below line while testing locally
        lastModificationTime = metadata.getLastModified();

    }

    public void loadS3ConfigurationData() {
        String s3ConfigurationAsString = s3Client.getObjectAsString(bucketName, key);
        //comment out above line while testing locally
        //String s3ConfigurationAsString = "{\"featureFlags\":{\"sterling\":true,\"corroborator\":true}}";
        log.info("Loaded config from Bucket={} Key={} and s3ConfigurationAsString={} ", bucketName, key,
                s3ConfigurationAsString.replaceAll("\\s*\\n\\s*", ""));

        try {
            S3ConfigurationModel s3Configuration = mapper.readValue(s3ConfigurationAsString, S3ConfigurationModel.class);
            featureFlag = s3Configuration.getFeatureFlags();
            log.info("Flags inline={} nikeid={}", featureFlag.getInline(), featureFlag.getNikeid());
        } catch (Exception exe) {
            log.error("Error while loading the configuration", exe);
        }
    }
}
