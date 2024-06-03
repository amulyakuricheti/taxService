package com.nike.mptaxmanager.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nike.mptaxmanager.exception.InvalidRulesEngineResponseException;
import com.nike.mptaxmanager.model.Order;
import com.nike.mptaxmanager.model.rulesengine.RulesEngineResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import java.io.IOException;

@Slf4j
public class CreateRulesEngineAggregrator implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        log.info("Control in RulesEngineValidation - aggregate()");
        ObjectMapper mapper = new ObjectMapper();
        String rulesEngineString = newExchange.getIn().getBody(String.class);
        Order order = oldExchange.getIn().getBody(Order.class);
        try {
            oldExchange.getIn().setHeader("EnterpriseCode", order.getEnterpriseCode());
            RulesEngineResponse rulesEngineResponse = mapper.readValue(rulesEngineString, RulesEngineResponse.class);
            oldExchange.getIn().setHeader("taxRecalcRequired", rulesEngineResponse.isTaxRecalcRequired());
            oldExchange.getIn().setHeader("taxEngine", rulesEngineResponse.getTaxEngine());
            log.info("RulesEngine response={} for order={}", rulesEngineResponse, order);
        } catch (IOException e) {
            log.error("Error occurred in CreateRulesEngineAggregator", e);
            throw new InvalidRulesEngineResponseException("Error occurred when mapping the rules engine response", e);
        }

        oldExchange.getIn().setBody(order);
        return oldExchange;
    }
}

