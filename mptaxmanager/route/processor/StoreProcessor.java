package com.nike.mptaxmanager.route.processor;

import com.nike.mptaxmanager.model.Order;
import com.nike.mptaxmanager.utils.CompanyCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.StringUtils;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class StoreProcessor implements Processor {
    public static final String Y = "Y";
    public static final String DEFAULT = "DEFAULT";
    public static final String NONMERCH = "NONMERCH";
    public static final String UNDERSCORE = "_";

    @Override
    public void process(Exchange exchange) {
        Order order = exchange.getIn().getBody(Order.class);
        String locale = order.getExtn().getExtnLocale();
        if (locale != null && !locale.isEmpty()) {
            order.setCompanyCode(CompanyCode.getStoreCompanyCode(locale.split(UNDERSCORE)[1]));
        }
        List<Order.OrderLines.OrderLine> orderLineList = order.getOrderLines().getOrderLine();
        for (Order.OrderLines.OrderLine orderLine : orderLineList) {
            if (orderLine.getLineExtn() != null && StringUtils.isNotBlank(orderLine.getLineExtn().getExtnCommodityCode())) {
                log.info("Setting sendCommodityCode to Y for lineType={}, enterpriseCode={}, orderType={} and orderNumber={}",
                        orderLine.getLineType(), order.getEnterpriseCode(), order.getOrderType(), order.getOrderNo());
                orderLine.setSendCommodityCode(Y);
            }
            if (orderLine.getLineExtn() != null && StringUtils.isBlank(orderLine.getLineExtn().getExtnCommodityCode())
                    && StringUtils.isNotBlank(orderLine.getLineExtn().getExtnProductId())) {
                log.info("Setting sendProductCode for to Y for lineType={}, enterpriseCode={}, orderType={} and orderNumber={}",
                        orderLine.getLineType(), order.getEnterpriseCode(), order.getOrderType(), order.getOrderNo());
                orderLine.setSendProductCode(Y);
            }
            if (orderLine.getItem() != null && hasTaxProductCode(orderLine)) {
                log.info("Setting sendTaxProductCode for to Y for lineType={}, enterpriseCode={}, orderType={} and orderNumber={}",
                        orderLine.getLineType(), order.getEnterpriseCode(), order.getOrderType(), order.getOrderNo());
                orderLine.setSendTaxProductCode(Y);
            }
            if (orderLine.getLineType() != null && orderLine.getLineType().equalsIgnoreCase(NONMERCH)) {
                log.info("Setting extnShipGroup for to Default for lineType={}, enterpriseCode={}, orderType={} and orderNumber={}",
                        orderLine.getLineType(), order.getEnterpriseCode(), order.getOrderType(), order.getOrderNo());
                orderLine.getLineExtn().setExtnShipGroup(DEFAULT);
            }
        }
        exchange.getIn().setBody(order);
        log.info("Successfully created request object for oneSource={} for orderNumber={}", order, order.getOrderNo());
    }

    private static boolean hasTaxProductCode(final Order.OrderLines.OrderLine orderLine) {
        return StringUtils.isNotBlank(orderLine.getItem().getTaxProductCode());
    }
}

