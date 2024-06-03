package com.nike.mptaxmanager.processor;

import com.nike.mptaxmanager.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ValidateAndDefaultExtnRef implements Processor {
    @Override
    public void process(Exchange exchange) {
        log.info("Validating and defaulting the value for extnReference4 in ValidateAndDefaultExtnRef for orderNumber={}", exchange.getIn().getHeader("orderNumber"));
        Order body = exchange.getIn().getBody(Order.class);
        body.getOrderLines().getOrderLine().forEach(
                orderLine -> {
                    if (orderLine.getLineTaxes() != null && orderLine.getLineTaxes().getLineTaxes() != null) {
                        orderLine.getLineTaxes().getLineTaxes().forEach(
                                lineTax -> {
                                    if (StringUtils.isBlank(lineTax.getExtn().getExtnReference4())) {
                                        lineTax.getExtn().setExtnReference4(StringUtils.EMPTY);
                                    }
                                }
                        );
                    }
                }
        );
        exchange.getIn().setBody(body);
    }
}

