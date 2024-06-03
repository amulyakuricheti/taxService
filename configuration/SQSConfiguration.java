package com.nike.invoiceshipmentwrkr.configuration;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.QueueNameExistsException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
@ConditionalOnProperty(value = "invoiceshipmentwrkr.enableSQSClient", matchIfMissing = true)
public class SQSConfiguration {
    private static final String DLQ_ARN = "QueueArn";

    @Value("${isLocal:false}")
    private boolean isLocal;

    @Value("${isLocalStack:false}")
    private boolean isLocalStack;

    private String sqsLocalStackEndpoint = "http://localhost:4566";

    @Value("${sqs.invoiceshipment.wrkr.queue.name}")
    private String invoiceshipmentwrkrqueue;

    @Value("${sqs.invoiceshipment.wrkr.dlq.queue.name}")
    private String invoiceshipmentwrkrdlqueue;

    @Value("${sqs.invoiceshipment.wrkr.nikeid.queue.name}")
    private String nikeIdInvoiceshipmentwrkrqueue;

    @Value("${sqs.invoiceshipment.wrkr.nikeid.dlq.queue.name}")
    private String nikeIdInvoiceshipmentwrkrdlqueue;

    @Value("${sqs.invoiceshipment.wrkr.bopis.queue.name}")
    private String invoiceshipmentwrkrnikeBopisqueue;

    @Value("${sqs.invoiceshipment.wrkr.bopis.dlq.queue.name}")
    private String invoiceshipmentwrkrnikeBopisdlqueue;

    private AmazonSQS sqs = null;

//    @Bean(name = "invoiceshipmentwrkrqueueUrl")
//    public String getInvoiceShipmentWrkrqueueUrl() {
//        sqs = amazonSQSClient();
//        if (sqs.getQueueUrl(invoiceshipmentwrkrqueue) != null) {
//            return sqs.getQueueUrl(invoiceshipmentwrkrqueue).getQueueUrl();
//        }   else {
//            return "";
//        }
//
//    }

    @Bean
    public AmazonSQS amazonSQSClient() {
        if (sqs != null) {
            return sqs;
        }
        configureSQS();
        return sqs;
    }

    private void configureSQS() {
        log.info("MessageSQSConfiguration.amazonSQSClient() isLocal={}", this.isLocal);
        if (isLocal) {
            val defaultRegion = "us-east-1";
            sqs = AmazonSQSClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(getLocalEndpoint(), defaultRegion))
                    .build();
            createQueue(invoiceshipmentwrkrdlqueue, null);
            createQueue(invoiceshipmentwrkrqueue, null);
            createQueue(nikeIdInvoiceshipmentwrkrdlqueue, null);
            createQueue(nikeIdInvoiceshipmentwrkrqueue, null);
            createQueue(invoiceshipmentwrkrnikeBopisdlqueue, null);
            createQueue(invoiceshipmentwrkrnikeBopisqueue, null);
        } else {
            sqs = AmazonSQSClientBuilder.defaultClient();
            createQueue(invoiceshipmentwrkrdlqueue, null);
            createQueue(invoiceshipmentwrkrqueue, invoiceshipmentwrkrdlqueue);
            createQueue(nikeIdInvoiceshipmentwrkrdlqueue, null);
            createQueue(nikeIdInvoiceshipmentwrkrqueue, nikeIdInvoiceshipmentwrkrdlqueue);
            createQueue(invoiceshipmentwrkrnikeBopisdlqueue, null);
            createQueue(invoiceshipmentwrkrnikeBopisqueue, invoiceshipmentwrkrnikeBopisdlqueue);
        }
    }

    String getLocalEndpoint() {
        if (isLocalStack) {
            return sqsLocalStackEndpoint;
        } else {
            return "http://localhost:4566";
        }
    }

    private void createQueue(String queueName, String dlQueueName) {
        try {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("VisibilityTimeout", "40");
            attributes.put("DelaySeconds", "5");
            attributes.put("ReceiveMessageWaitTimeSeconds", "0");
            if (StringUtils.isNotEmpty(dlQueueName)) {
                GetQueueAttributesRequest queueAttributesRequest = new GetQueueAttributesRequest(
                        getQueueURL(dlQueueName))
                        .withAttributeNames(DLQ_ARN);
                Map<String, String> sqsAttributeMap = sqs.getQueueAttributes(queueAttributesRequest).getAttributes();
                log.debug("DLQ ARN " + sqsAttributeMap.get(DLQ_ARN));
                String redrivePolicy = "{\"maxReceiveCount\":5, \"deadLetterTargetArn\":\"" + sqsAttributeMap.get(DLQ_ARN) + "\"}";
                attributes.put("RedrivePolicy", redrivePolicy);
            }
            CreateQueueRequest queueRequest = new CreateQueueRequest()
                    .withQueueName(queueName)
                    .withAttributes(attributes);
            sqs.createQueue(queueRequest);
            log.info("{}, queue creation successful ", queueName);
        } catch (QueueNameExistsException e) {
            log.warn("{} Queue already exists. Exception is : {} ", queueName, e);
        } catch (Exception e) {
            log.error("{} Unknown Exception is : {} ", queueName, e);
        }
    }

    public String getQueueURL(final String queueName) {
        return sqs.getQueueUrl(queueName).getQueueUrl();
    }
}
