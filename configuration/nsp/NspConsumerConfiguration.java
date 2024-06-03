package com.nike.invoiceshipmentwrkr.configuration.nsp;

import com.nike.om.nsp.routes.NspRouteBuilder;
import com.nike.om.nsp.utils.NspKafkaConfig;

import com.nike.invoiceshipmentwrkr.configuration.InvoiceShipmentWrkrRouteConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class NspConsumerConfiguration {

    private final NspRouteBuilder nspRouteBuilder;
    private final CamelContext context;
    private final NspKafkaConfig nspKafkaConfig;

    @Value("${custom.kafka.topic}")
    private String nspConsumerTopic;

    @Value("${nike.neon.kafka.broker}")
    private String brokerUrl;

    public static final String DIRECT_PROCESS_FROM_SQS_ROUTE_ID = "ProcessFromSQS";

    public static final String TAX_INGRESS_ROUTE_NAME = "direct:" + DIRECT_PROCESS_FROM_SQS_ROUTE_ID;

    @Bean("nspTaxConfig")
    public KafkaConfiguration nspTaxConfig() {
        return nspKafkaConfig.getConsumerConfigNsp(brokerUrl, nspConsumerTopic);
    }

    @PostConstruct
    public void addNspRoutes() throws Exception {
        final RouteBuilder nspScanfileConsumerRoute = nspRouteBuilder.nspConsumerRouteBuilder(nspConsumerTopic, "nspTaxConfig", DIRECT_PROCESS_FROM_SQS_ROUTE_ID);
        context.addRoutes(nspScanfileConsumerRoute);
    }

}