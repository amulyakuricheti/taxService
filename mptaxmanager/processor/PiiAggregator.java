package com.nike.mptaxmanager.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nike.mptaxmanager.exception.InvalidPIIResponseException;
import com.nike.mptaxmanager.model.Order;
import com.nike.mptaxmanager.model.PIIResponse;
import com.nike.mptaxmanager.model.PIIServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import java.util.List;

@Slf4j
public class PiiAggregator implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        log.info("PiiAggregator newexchange response={}", newExchange.getIn().getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        String object = "{\"objects\":[{\"id\":\"2503738e-acb1-42d8-ba73-6b52d061831e\",\"address\":{\"address1\":\"1180 N Brightleaf Blvd\",\"address2\":\"\",\"address3\":\"\",\"address4\":\"\",\"city\":\"Smithfield\",\"latitude\":\"35.5184783935547\",\"longitude\":\"-78.3165512084961\",\"country\":\"US\",\"state\":\"NC\",\"zipCode\":\"27577\"},\"recipient\":{\"firstName\":\"Tyiesha\",\"lastName\":\"Stone\",\"alternateLastName\":\"Stone\"},\"contactInformation\":{\"dayPhoneNumber\":\"9196317756\",\"email\":\"tyiesha.stone@johnstonnc.com\",\"phoneNumber\":{\"countryCode\":\"1\",\"subscriberNumber\":\"9196317756\"}},\"orderNumber\":\"C01398490953\",\"addressType\":\"SHIP_TO\",\"orderLineNumbers\":[1],\"resourceType\":\"order_mgmt/addresses\",\"links\":{\"self\":{\"ref\":\"order_mgmt/addresses/v1/2503738e-acb1-42d8-ba73-6b52d061831e\"}}}]}"; // Static JSON response
        try {
            PIIServiceResponse piiServiceResponse = objectMapper.readValue(object, PIIServiceResponse.class);
            log.info("Pii service response={}", piiServiceResponse);

            Order order = oldExchange.getIn().getBody(Order.class);
            List<Order.OrderLines.OrderLine> orderLineList = order.getOrderLines().getOrderLine();
            Order.CurrentShipmentShipFrom.ShipNode.PersonInfo personInfoMarkFor = null;
            Order.CurrentShipmentShipFrom.ShipNode.PersonInfo personInfoShipTo = null;

            for (PIIResponse piiResponse : piiServiceResponse.getObjects()) {
                for (Order.OrderLines.OrderLine orderLine : orderLineList) {
                    personInfoMarkFor = orderLine.getPersonInfoMarkFor();
                    personInfoShipTo = orderLine.getPersonInfoShipTo();
                    log.info("Assigning Pii address to orderLine, lineType={}, enterpriseCode={}, orderType={}, orderNumber={}",
                            orderLine.getLineType(), order.getEnterpriseCode(), order.getOrderType(), order.getOrderNo());
                    assignPiiAddresses(personInfoMarkFor, piiResponse);
                    assignPiiAddresses(personInfoShipTo, piiResponse);
                }
            }
            //set countryCode header for TCC service call
            if (personInfoShipTo != null && personInfoShipTo.getCountry() != null) {
                oldExchange.getIn().setHeader("countryCode", personInfoShipTo.getCountry());
            }
            oldExchange.getIn().setBody(order);
        } catch (Exception err) {
            log.error("Exception occurred while aggregating PII response");
            throw new InvalidPIIResponseException("Exception occurred while aggregating PII response. Error={}", err);
        }
        return oldExchange;
    }

    private void assignPiiAddresses(Order.CurrentShipmentShipFrom.ShipNode.PersonInfo personInfo, PIIResponse piiResponse) {
        if (personInfo != null && personInfo.getAddressID() != null && personInfo.getAddressID().equals(piiResponse.getId())) {
            personInfo.setCity(piiResponse.getAddress().getCity());
            personInfo.setZipCode(piiResponse.getAddress().getZipCode());
            personInfo.setState(piiResponse.getAddress().getState());
            personInfo.setCountry(piiResponse.getAddress().getCountry());
        }
    }
}

