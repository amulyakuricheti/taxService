package com.nike.invoiceshipmentwrkr.configuration;

import com.nike.cerberus.client.CerberusClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CerberusConfig {
    private final CerberusClient cerberusClient;

    @Value("${nike.neon.cerberus.sdbPath}")
    private String credsSdbPath;

    @Bean("cerberusProps")
    public Map<String, String> loadCerberusProperties() {
        return cerberusClient.read(credsSdbPath).getData();
    }

}
