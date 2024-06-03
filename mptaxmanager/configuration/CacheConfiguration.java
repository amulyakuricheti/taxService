package com.nike.mptaxmanager.configuration;

import com.nike.mptaxmanager.service.S3ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableScheduling
@ConditionalOnProperty(value = "mptaxmanager.enableS3Scheduler", matchIfMissing = true)
public class CacheConfiguration {

    @Autowired
    private S3ConfigurationService s3ConfigurationService;

    @Scheduled(fixedRate = 60000, initialDelay = 0)
    public void setCache() {
        s3ConfigurationService.loadCache();
    }
}

