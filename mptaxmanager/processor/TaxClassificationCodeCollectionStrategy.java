package com.nike.mptaxmanager.processor;

import com.nike.mptaxmanager.cache.TaxManagerCache;
import com.nike.mptaxmanager.model.Order;
import com.nike.mptaxmanager.model.Order.OrderLines.OrderLine;
import com.nike.mptaxmanager.model.Order.OrderLines.OrderLine.Item;
import com.nike.mptaxmanager.model.taxClassificationEngine.TCCResponse.ClassificationCode;
import java.util.HashMap;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaxClassificationCodeCollectionStrategy {

    @Autowired
    private final TaxManagerCache taxManagerCache;

    @Value("${tcc.shipping.charge.gtin:00000000000006}")
    private String tscGtin;

    @Value("${tcc.default.productcode}")
    private String defaultProductCode;
    /**
     * This method is used for binding the tax product code post entire TCC call completion.
     * @param exchange
     * @return order
     * @throws Exception
     */
    public Order process(Exchange exchange) throws Exception {
        log.info("Tax Classification Code collection for orderNumber={}", exchange.getIn().getHeader("orderNumber"));
        Order order = exchange.getIn().getBody(Order.class);
        log.info("Tax Classification Code collection for order data={}", order);
        HashMap<String, String> classificationCodeMap = new HashMap<>();
        for (OrderLine orderLine : order.getOrderLines().getOrderLine()) {
            Item item = orderLine.getItem();
            if (item != null && item.getUpcCode() != null && !item.getUpcCode().isEmpty()) {
                ClassificationCode classificationCode = taxManagerCache.findClassificationCodeByUPCCode(item.getUpcCode());
                if (classificationCode != null) {
                    OrderLine.ItemDetails.ClassificationCodes classificationCodes = new OrderLine.ItemDetails.ClassificationCodes();
                    classificationCodes.setTaxProductCode(classificationCode.getClassification());
                    orderLine.getItem().setTaxProductCode(classificationCode.getClassification());
                    orderLine.getItemDetails().setClassificationCodes(classificationCodes);
                } else {
                    OrderLine.ItemDetails.ClassificationCodes classificationCodes = new OrderLine.ItemDetails.ClassificationCodes();
                    classificationCodes.setTaxProductCode(defaultProductCode);
                    orderLine.getItem().setTaxProductCode(defaultProductCode);
                    orderLine.getItemDetails().setClassificationCodes(classificationCodes);
                }
                if (!StringUtils.isEmpty(orderLine.getItem().getTaxProductCode())) {
                    classificationCodeMap.put(orderLine.getOrderLineKey(), orderLine.getItem().getTaxProductCode());
                }
            }
        }

        log.info("Validating HeaderCharges and Setting the ProductCode for HeaderChargeGtin");
        if (Objects.nonNull(order.getHeaderCharges()) && Objects.nonNull(order.getHeaderCharges().getHeaderCharges())) {
            order.getHeaderCharges().getHeaderCharges().stream().forEach(headerCharge -> {
                ClassificationCode cfCode = taxManagerCache.findClassificationCodeByUPCCode(tscGtin);
                if (Objects.nonNull(cfCode)) {
                    classificationCodeMap.put(tscGtin, cfCode.getClassification());
                    headerCharge.setProductCode(cfCode.getClassification());
                } else {
                    classificationCodeMap.put(tscGtin, defaultProductCode);
                    headerCharge.setProductCode(defaultProductCode);
                }
            });
        }

        exchange.setProperty("classificationCode", classificationCodeMap);
        log.info("Mapped classification details for orderNumber={}, with Gtin-productCode as {}", exchange.getIn().getHeader("orderNumber"), classificationCodeMap);
        return order;
    }
}

