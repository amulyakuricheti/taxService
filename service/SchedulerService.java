package com.nike.invoiceshipmentwrkr.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
public class SchedulerService {
    @Autowired
    private ProducerTemplate producerTemplate;
    @Scheduled(cron = "${cron.utc}", zone = "UTC")
    public void triggerTimeZoneUTC()  {
        final long startTime = System.currentTimeMillis();
        producerTemplate.requestBody("direct:scheduledTaxShipmentRoute", "UTC");
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        log.info("Execution time for UTC Time Zone ={} seconds", seconds);
    }
}
