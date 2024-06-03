package com.nike.mptaxmanager.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nike.mptaxmanager.cache.TaxManagerCache;
import com.nike.mptaxmanager.exception.InvalidTCCResponseException;
import com.nike.mptaxmanager.model.taxClassificationEngine.TCCResponse;
import com.nike.mptaxmanager.model.taxClassificationEngine.TCCResponse.ClassificationCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class TaxClassificationCodeAggregation implements AggregationStrategy {
    private static ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private TaxManagerCache taxManagerCache;

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        log.info("Tax Classification Code Aggregation method to update cache value with oldexchaneh :: {}", oldExchange);
        boolean tccCallFailed = "mptaxmanager-TCCCall".equalsIgnoreCase(newExchange.getProperty("CamelFailureRouteId", String.class));
        if (!tccCallFailed) {
            String tccResponseString = newExchange.getIn().getBody(String.class);
            TCCResponse tccResponse;

            try {
                tccResponse = objectMapper.readValue(tccResponseString, TCCResponse.class);
                log.info("Successfully mapped TCC response={} in cache", tccResponse);
            } catch (Exception e) {
                log.error("Error in parsing the TCC response={} with error={}", tccResponseString, e);
                throw new InvalidTCCResponseException("Invalid TCC Response when mapping to TCCResponse model. Error={}", e);
            }

            stampTaxClassificationCode(tccResponse);

       //     String object = oldExchange.getIn().getBody(String.class);

        }
        log.info("oldExchange body={}", oldExchange);
        return oldExchange;
    }

    private void stampTaxClassificationCode(final TCCResponse tccResponse) {
        if (tccResponse != null && tccResponse.getClassificationCodes() != null) {
            for (ClassificationCode classificationCode : tccResponse.getClassificationCodes()) {
                taxManagerCache.putClassificationCodeByUPCCode(classificationCode.getGtin(), classificationCode);
            }
        }
    }
}

