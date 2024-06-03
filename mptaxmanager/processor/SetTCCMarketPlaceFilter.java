package com.nike.mptaxmanager.processor;

import com.nike.mptaxmanager.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.StringUtils;

@Slf4j
public class SetTCCMarketPlaceFilter implements Processor {

    @Override
    public void process(Exchange exchange) {
        log.info("Control in SetTCCMarketPlaceFilter - process()");
        Order order = exchange.getIn().getBody(Order.class);
        for (Order.OrderLines.OrderLine orderLine : order.getOrderLines().getOrderLine()) {
            if (orderLine.getPersonInfoShipTo() != null && StringUtils.isNotBlank(orderLine.getPersonInfoShipTo().getCountry())) {
                exchange.getIn().setHeader("countryCode", orderLine.getPersonInfoShipTo().getCountry());
                break;
            }
        }
    }
}

