package com.nike.mptaxmanager.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nike.mptaxmanager.cache.TaxManagerCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class S3ConfigurationService {

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private TaxManagerCache cache;

    @Value("${com.nike.s3.mptaxmanager.configuration.key}")
    private String key;
    private Date lastModificationTime = null;

    @Value("#{${envBucketMap:{prod: 'nike-commerce-prod-app-internal', test: 'nike-commerce-test-app-internal'}}}")
    private Map<String, String> envBucketMap;

    @Value("${com.nike.s3.mptaxmanager.configuration.bucket.name:#{null}}")
    private String bucketName;

    private Map<String, String>   commodityCodes;

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${com.nike.s3.local:false}")
    private boolean useLocalS3;

    @Autowired
    public S3ConfigurationService(@Value("${com.nike.s3.mptaxmanager.configuration.bucket}") String bucketName, @Value("${com.nike.s3.mptaxmanager.configuration.key}") String key) {
        this.bucketName = bucketName;
        this.key = key;
    }

    public Map<String, String> getCommodityCodes() {
        if (null == commodityCodes) {
            loadCache();
        }
        return commodityCodes;
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
        String s3ConfigurationAsString;
        if (useLocalS3) {
            s3ConfigurationAsString = "{\n" + "  \"23\": \"\",\n" + "  \"24\": \"\"\n" + "\n" + "}";
        } else {
            s3ConfigurationAsString = s3Client.getObjectAsString(bucketName, key);
        }
        log.info("Loaded config from Bucket={} Key={} and s3ConfigurationAsString={} ", bucketName, key, s3ConfigurationAsString.replaceAll("\\s*\\n\\s*", ""));
        try {
            commodityCodes = mapper.readValue(s3ConfigurationAsString, HashMap.class);
            log.info("Commodity Codes={}", commodityCodes.toString());
            commodityCodes.entrySet().stream()
                    .forEach(e -> cache.putCommodityCodeByGtin(e.getKey(), e.getValue()));
        } catch (Exception exe) {
            log.error("Error while loading the configuration", exe);
        }
    }
}

