package com.nike.mptaxmanager.configuration.route;

import com.nike.cpe.cerberus.client.auth.DefaultCerberusCredentialsProviderChain;
import com.nike.cpe.vault.client.StaticVaultUrlResolver;
import com.nike.cpe.vault.client.VaultClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class CerberusClientConfiguration {


    @Value("${cerberus.url}")
    private String cerberusUrl;

    @Bean
    public VaultClient vaultClient() {
        final StaticVaultUrlResolver urlResolver = new StaticVaultUrlResolver(cerberusUrl);

        return new VaultClient(urlResolver, new DefaultCerberusCredentialsProviderChain(urlResolver));
    }

}

