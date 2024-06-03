package com.nike.mptaxmanager.processor;

import com.nike.internal.util.StringUtils;
import com.nike.mptaxmanager.model.Order;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class EnhanceTMPayloadProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        Order body = exchange.getIn().getBody(Order.class);
        final String orderNumber = body.getOrderNo();
        List<Order.OrderLines.OrderLine> orderLineList = body.getOrderLines().getOrderLine();
        orderLineList.stream()
                .filter(orderLine -> (orderLine.getLineExtn() != null && StringUtils.isBlank(orderLine.getLineExtn().getExtnProductId())))
                .map(orderLine -> orderLine.getLineExtn())
                .forEach(extn -> extn.setExtnProductId("999999"));
        log.info("EnhanceTMPayloadProcessor: set ExtnProductId for orderNumber={}", orderNumber);
    }

}

