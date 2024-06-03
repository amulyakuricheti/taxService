package com.nike.invoiceshipmentwrkr.configuration;

import com.nike.invoiceshipmentwrkr.service.S3ConfigurationServiceShipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableScheduling
public class CacheConfigurationShipment {

    @Autowired
    private S3ConfigurationServiceShipment s3ConfigurationService;

    @Scheduled(fixedRate = 60000, initialDelay = 0)
    public void setCache() {
        s3ConfigurationService.loadCache();
    }
}
