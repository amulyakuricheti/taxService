//package com.nike.invoiceshipmentwrkr.configuration;
//
//import com.nike.hades.nspkafkautil.model.KafkaSourceConfig;
//import com.nike.hades.nspkafkautil.model.OauthKeys;
//import com.nike.hades.nspkafkautil.producer.PublishToKafkaProducer;
//
//import com.nike.om.nsp.routes.NspRouteBuilder;
//import com.nike.om.nsp.utils.NspKafkaConfig;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.camel.CamelContext;
//import org.apache.camel.builder.RouteBuilder;
//import org.apache.camel.component.kafka.KafkaConfiguration;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.event.EventListener;
//
//import javax.annotation.PostConstruct;
//import java.util.Map;
//
//@Configuration
//@Slf4j
//public class NSPConfiguration {
//
//    private final NspRouteBuilder nspRouteBuilder;
//    private final CamelContext context;
//    private final NspKafkaConfig nspKafkaConfig;
//    private final PublishToKafkaProducer publishToKafkaProducer;
//
//    @Value("${kafka.tax.events.topic}")
//    private String nspTaxEventConsumerTopic;
//
//    @Value("${nsp.tax.group.id}")
//    private String nspTaxEventGroupId;
//
//    @Value("${kafka.broker.url}")
//    private String brokerUrl;
//    @Value("${kafka.stream.clientid}")
//    private String sourceId;
//    @Value("${kafka.topicName}")
//    private String topicName;
//
//    public static final String OAUTH_SECRET_KEY = "client_secret";
//    public static final String OAUTH_CLIENT_ID_KEY = "client_id";
//    public static final String OAUTH_TOKEN_URL_KEY = "token_url";
//
//    public static final String NSP_CONSUMER_ROUTE_ID = "nsp-tax-event-consumer-route-id";
//    public static final String NSP_CONSUMER_ROUTE = "direct:" + NSP_CONSUMER_ROUTE_ID;
//    public static final String NSP_GET_TAX_EVENT_ROUTE_ID = "get-tax-event-from-nsp-route-id";
//
//    @Autowired
//    private Map<String, String> cerberusProps;
//
//    @Autowired
//    public NSPConfiguration(CamelContext camelContext,
//                            NspRouteBuilder nspRouteBuilder, NspKafkaConfig nspKafkaConfig, PublishToKafkaProducer publishToKafkaProducer) {
//        this.nspRouteBuilder = nspRouteBuilder;
//        this.context = camelContext;
//        this.nspKafkaConfig = nspKafkaConfig;
//        this.publishToKafkaProducer = publishToKafkaProducer;
//    }
//
//    @Bean("nspConsumerConfig1")
//    public KafkaConfiguration nspConsumerConfig() {
//        KafkaConfiguration consumerConfig = nspKafkaConfig.getConsumerConfig(nspTaxEventConsumerTopic, nspTaxEventGroupId);
//        return consumerConfig;
//    }
//
//    @PostConstruct
//    public void addNspRoutes() throws Exception {
//        final RouteBuilder nspTaxEventConsumerRoute = nspRouteBuilder.nspConsumerRouteBuilder(nspTaxEventConsumerTopic, "nspConsumerConfig1",
//                NSP_CONSUMER_ROUTE, NSP_CONSUMER_ROUTE_ID, NSP_GET_TAX_EVENT_ROUTE_ID);
//        context.addRoutes(nspTaxEventConsumerRoute);
//    }
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void initKafkaProducer() {
//        publishToKafkaProducer.initProducer(buildOauthKeys(), buildKafkaSource());
//        log.info("action=processNSP status=Successfully initialized PublishToKafkaProducer.");
//    }
//
//    private OauthKeys buildOauthKeys() {
//        if (cerberusProps.get(OAUTH_TOKEN_URL_KEY) != null && cerberusProps.get(OAUTH_CLIENT_ID_KEY) != null
//                && cerberusProps.get(OAUTH_SECRET_KEY) != null) {
//            log.info("action=processNSP event: retrievalOfCerberusValues is Successful");
//        } else {
//            log.error("action=processNSP event: retrievalOfCerberusValues Failed");
//        }
//        return OauthKeys.builder()
//                .tokenUrl(cerberusProps.get(OAUTH_TOKEN_URL_KEY))
//                .clientId(cerberusProps.get(OAUTH_CLIENT_ID_KEY))
//                .clientSecret(cerberusProps.get(OAUTH_SECRET_KEY))
//                .build();
//    }
//
//    private KafkaSourceConfig buildKafkaSource() {
//        return KafkaSourceConfig.builder()
//                .sourceBrokerUrl(brokerUrl)
//                .sourceId(sourceId)
//                .sourceTopicName(topicName)
//                .build();
//    }
//
//
//
//
//}
