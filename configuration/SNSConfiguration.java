//package com.nike.invoiceshipmentwrkr.configuration;
//
//import com.amazonaws.client.builder.AwsClientBuilder;
//import com.amazonaws.services.sns.AmazonSNS;
//import com.amazonaws.services.sns.AmazonSNSClientBuilder;
//import com.amazonaws.services.sns.model.CreateTopicResult;
//import com.amazonaws.services.sns.model.ListTopicsResult;
//import com.amazonaws.services.sns.model.Topic;
//import com.amazonaws.services.sns.util.Topics;
//import com.amazonaws.services.sqs.AmazonSQS;
//import lombok.extern.slf4j.Slf4j;
//import lombok.val;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.List;
//
//@Configuration
//@Slf4j
//@ConditionalOnProperty(value = "invoiceshipmentwrkr.enableSNSClient", matchIfMissing = true)
//public class SNSConfiguration {
//    private boolean isLocal;
//    private boolean isLocalStack;
//    private String localStackSNSEndpoint;
//    private String snsTopicName;
//    private String nikeIdSnsTopicName;
//    private String nikeBopisSnsTopicName;
//    private String invoiceShipmentWrkrQueueUrl;
//    private String nikeIdInvoiceShipmentWrkrQueueUrl;
//    private String invoiceShipmentWrkrNikeBopisQueueUrl;
//
//    @Value("${sqs.invoiceshipment.wrkr.queue.name}")
//    private String invoiceshipmentwrkrqueue;
//
//    @Value("${sqs.invoiceshipment.wrkr.nikeid.queue.name}")
//    private String nikeIdInvoiceshipmentwrkrqueue;
//
//    @Value("${sqs.invoiceshipment.wrkr.bopis.queue.name}")
//    private String invoiceshipmentwrkrnikeBopisqueue;
//
//    @Autowired
//    private AmazonSQS amazonSQS;
//
//    private AmazonSNS amazonSNS;
//
//    // CHECKSTYLE IGNORE HiddenField
//    // allow endpoints etc. to be passed into constructor for use with LocalstackDockerTestRunner
//    @Bean
//    public AmazonSNS amazonSNSClient(@Value("${isLocal:false}") boolean isLocal,
//                                     @Value("${isLocalStack:false}") boolean isLocalStack,
//                                     @Value("${localStackSNSEndpoint:http://localhost:4566}") String localStackSNSEndpoint,
//                                     @Autowired AmazonSQS amazonSQS,
//                                     @Value("${sns.topic.name}") String snsTopicName,
//                                     @Value("${nikeid.sns.topic.name}") String nikeIdSnsTopicName,
//                                     @Value("${nikekr.sns.topic.name}") String nikeBopisSnsTopicName) {
////                                     @Autowired @Qualifier("invoiceshipmentwrkrqueueUrl") String invoiceShipmentWrkrQueueUrl) {
//        this.isLocal = isLocal;
//        this.isLocalStack = isLocalStack;
//        this.localStackSNSEndpoint = localStackSNSEndpoint;
//        this.amazonSQS = amazonSQS;
//        this.snsTopicName = snsTopicName;
//        this.nikeIdSnsTopicName = nikeIdSnsTopicName;
//        this.nikeBopisSnsTopicName = nikeBopisSnsTopicName;
//        this.invoiceShipmentWrkrQueueUrl = amazonSQS.getQueueUrl(invoiceshipmentwrkrqueue).getQueueUrl();
//        this.nikeIdInvoiceShipmentWrkrQueueUrl = amazonSQS.getQueueUrl(nikeIdInvoiceshipmentwrkrqueue).getQueueUrl();
//        this.invoiceShipmentWrkrNikeBopisQueueUrl = amazonSQS.getQueueUrl(invoiceshipmentwrkrnikeBopisqueue).getQueueUrl();
//
//        if (this.isLocal) {
//            log.info("isLocal = true");
//            amazonSNS = createAmazonSNSForLocalEnvironment();
//        } else {
//            log.info("isLocal = false");
//            amazonSNS = AmazonSNSClientBuilder.defaultClient();
//        }
//
//        if (amazonSNS != null) {
//            configureSNS();
//        }
//
//        return amazonSNS;
//    }
//    // CHECKSTYLE END IGNORE HiddenField
//
//    private AmazonSNS createAmazonSNSForLocalEnvironment() {
//        if (isLocalStack) {
//            log.info("Assuming that localstack is providing a local SNS service, because isLocalStack=true");
//            return createLocalStackAmazonSNS();
//        } else {
//            log.info("Assuming that there is no local SNS service available, because isLocalStack=false");
//            return null;
//        }
//    }
//
//    private void configureSNS() {
//        log.info("Running the configurations for SNS");
//        createSNSSubscription(snsTopicName, invoiceShipmentWrkrQueueUrl);
//        log.info("Running the configurations for NikeId SNS");
//        createSNSSubscription(nikeIdSnsTopicName, nikeIdInvoiceShipmentWrkrQueueUrl);
//        log.info("Running the configurations for NIKE Bopis SNS");
//        createSNSSubscription(nikeBopisSnsTopicName, invoiceShipmentWrkrNikeBopisQueueUrl);
//    }
//
//    private void createSNSSubscription(String topicName, String queueUrl) {
//        String topicArn = getTopic(topicName, amazonSNS);
//
//        if (topicArn == null && isLocal && isLocalStack) {
//            log.info("Creating topicName {} on localstack SNS because isLocalStack=true", topicName);
//            CreateTopicResult topic = amazonSNS.createTopic(topicName);
//            topicArn = topic.getTopicArn();
//            log.info("Created topic {} locally: ", topicArn);
//        }
//
//        if (topicArn == null) {
//            log.error("topicArn is null");
//            throw new NullPointerException("topicArn is null");
//        }
//
//        log.info("Topic {} is in place.", topicArn);
//        String subscriptionArn = Topics.subscribeQueue(amazonSNS, amazonSQS, topicArn, queueUrl);
//        log.info("Subscribed to topic {} with resulting subscription ARN: {}", topicArn, subscriptionArn);
//    }
//
//    private AmazonSNS createLocalStackAmazonSNS() {
//        val defaultRegion = "us-east-1";
//        AmazonSNS localStackAmazonSNS = AmazonSNSClientBuilder.standard()
//                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localStackSNSEndpoint,
//                        defaultRegion))
//                .build();
//        return localStackAmazonSNS;
//    }
//
//    public static String getTopic(String topicName, AmazonSNS amazonSNS) {
//        String topicArn = null;
//        String nextToken = null;
//        do {
//            ListTopicsResult listTopics;
//            if (nextToken == null) {
//                listTopics = amazonSNS.listTopics();
//            } else {
//                listTopics = amazonSNS.listTopics(nextToken);
//            }
//            List<Topic> topics = listTopics.getTopics();
//            for (Topic topic : topics) {
//                if (topic.getTopicArn().endsWith(topicName)) {
//                    topicArn = topic.getTopicArn();
//                    log.info("Topic {} exists:", topicArn);
//                    break;
//                }
//            }
//            nextToken = listTopics.getNextToken();
//        } while (topicArn == null && nextToken != null);
//
//        return topicArn;
//    }
//}
