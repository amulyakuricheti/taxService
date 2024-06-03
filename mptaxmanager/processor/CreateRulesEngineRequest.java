package com.nike.mptaxmanager.processor;

import com.nike.mptaxmanager.model.Order;
import com.nike.mptaxmanager.model.rulesengine.RulesEngineRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Body;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

/**
 * This class creates the Rules Engine request required to call Post endpoint
 */
@Slf4j
public class CreateRulesEngineRequest {

    public void createRulesEngineRequest(@Body Message message) {
        log.info("Control in createRulesEngineRequest - createRulesEngineRequest()");
        Order order = message.getBody(Order.class);
        List<Order.References.Reference> references = order.getReferences().getReferences();
        String taxRemittedFlag = "";
        String channel = "";
        String appId = "";
        RulesEngineRequest request = RulesEngineRequest.builder()
                .omsRegionReference(order.getEnterpriseCode())
                .build();

        if (references != null) {
            for (Order.References.Reference reference : references) {
                String name = reference.getName();
                if (name.equalsIgnoreCase("AppId")) {
                    appId = reference.getValue();
                }
                if (name.equalsIgnoreCase("TaxRemittedFlag")) {
                    taxRemittedFlag = reference.getValue();
                }
                if (name.equalsIgnoreCase("Channel")) {
                    channel  = reference.getValue();
                }
            }
        }

        if (StringUtils.isNotBlank(channel)) {
            request.setChannel(channel);
            request.setAppId("");
            request.setOrderType("SALES_ORDER");
            //This is required once we move to thinOM
            //request.setOrderType(order.getOrderType());
        } else {
            request.setOrderClassification("STORE".equalsIgnoreCase(order.getOrderType()) ? "STORE" : "STANDARD");
            request.setChannel("");

            if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(taxRemittedFlag)) {
                request.setAppId(appId);
            } else  {
                request.setAppId("");
            }
        }
        if (StringUtils.isNotBlank(taxRemittedFlag)) {
            request.setTaxRemitted("Y".equalsIgnoreCase(taxRemittedFlag) ? true : false);
            request.setDraftOrder(false);
            request.setNewOrder(false);
        } else {
            request.setNewOrder("Y".equalsIgnoreCase(order.getIsNewOrder()) ? true : false);
            request.setDraftOrder("Y".equalsIgnoreCase(order.getDraftOrderFlag()) ? true : false);
            request.setTaxRemitted(false);
        }
        message.setBody(request);
        log.info("Successfully created rules engine request!!");
    }
}

