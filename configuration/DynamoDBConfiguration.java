package com.nike.invoiceshipmentwrkr.configuration;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.nike.dynamodb.DefaultDynamoDBClientFactory;
import com.nike.dynamodb.DynamoDBConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Configuration
@Slf4j
@ConditionalOnProperty(value = "invoiceshipmentwrkr.enableDynamoClient", matchIfMissing = true)
public class DynamoDBConfiguration {
    @Value("${dynamodb.config.local:false}")
    private boolean isDBLocal;

    @Value("${dynamodb.config.domain}")
    private String domain;

    @Value("${isLocalStack:false}")
    private boolean isLocalStack;

    @Value("${dynamodb.config.localstack.host:http://localhost:4566}")
    private String localStackDynamoEndpoint;

    @Value("${dynamodb.config.local.host:http://localhost:7999}")
    private String localDynamoEndpoint;

    @Bean
    public DynamoDBMapper provideAmazonDynamoDBMapper() {
        val config = getDynamoDBConfig();
        val client = new DefaultDynamoDBClientFactory(config).lowLevelAsyncClient();
        return new DynamoDBMapper(client);
    }

    private DynamoDBConfig getDynamoDBConfig() {
        if (isDBLocal || isLocalStack) {
            log.info("Recent changes");
            return DynamoDBConfig
                    .builder()
                    .domain(domain)
                    .endpoint(getLocalEndpoint())
                    .build();
        }
        DynamoDBConfig.useTypesafeConfig(getAppConfig());
        return DynamoDBConfig.builder().domain(domain).build();
    }

    private Config getAppConfig() {
        val profile = System.getProperty("spring.profiles.active");
        final String envProperties = "application-" + Optional.ofNullable(profile).orElse("local");
        return ConfigFactory.load(envProperties).withFallback(ConfigFactory.load());
    }

    String getLocalEndpoint() {
        if (isLocalStack) {
            return localStackDynamoEndpoint;
        } else {
            return localDynamoEndpoint;
        }
    }

    /**
     * Spring Bean which initialize or configures the DynamoDB connection.
     * If the isDBLocal is true, then the DynamoDb locally running DB is used , otherwise the aws dynamodb is used.
     *
     * @return AmazonDynamoDB object
     */
    @Bean
    public AmazonDynamoDB provideAmazonDynamoDB() {
        Config typeSafeConfig = getAppConfig();
        DynamoDBConfig dynamoDBConfig;
        if (isDBLocal || isLocalStack) {
            dynamoDBConfig = DynamoDBConfig.builder().domain(domain).endpoint(getLocalEndpoint()).build();
            dynamoDBConfig.useTypesafeConfig(typeSafeConfig);
            return new DefaultDynamoDBClientFactory(dynamoDBConfig).lowLevelClient();
        } else {
            dynamoDBConfig = DynamoDBConfig.builder().domain(domain).build();
            dynamoDBConfig.useTypesafeConfig(typeSafeConfig);
            return new DefaultDynamoDBClientFactory(dynamoDBConfig.fromProperties(new DefaultAWSCredentialsProviderChain())).lowLevelAsyncClient();
        }
    }

    @Bean
    public RestTemplate getDefaultRestTemplate() {
        final RestTemplate template = new RestTemplate();
        return template;
    }
}
