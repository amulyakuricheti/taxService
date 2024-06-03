package com.nike.mptaxmanager.processor;

import com.nike.internal.util.StringUtils;
import com.nike.mptaxmanager.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class HeadersConfigurer implements Processor {

    // UPC_SPLIT_SIZE for aggregating the UPC codes
    public static final int UPC_SPLIT_SIZE = 25;

    @Value("${tcc.shipping.charge.gtin:00000000000006}")
    private String tscGtin;

    @Override
    public void process(Exchange exchange) {
        log.info("Setting Headers in HeadersConfigurer for orderNumber={}", exchange.getIn().getHeader("orderNumber"));

        Order body = exchange.getIn().getBody(Order.class);
        final String orderNumber = body.getOrderNo();
        Map<String, Object> headers = new HashMap<>();
        String uuid = UUID.randomUUID().toString();

        headers.put("orderNumber", orderNumber);
        headers.put("requestTimeStamp", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(ZonedDateTime.now()));
        headers.put("OrderType", body.getOrderType());
        headers.put("Id", uuid + orderNumber);
        headers.put("ShipmentKey", body.getCurrentShipmentShipFrom().getShipmentKey());
        headers.put("EnterpriseCode", body.getEnterpriseCode());
        headers.put("isNewOrder", body.getIsNewOrder());
        headers.put("taxExemptFlag", body.getTaxExemptFlag());
        headers.put("draftOrderFlag", body.getDraftOrderFlag());
        setReferenceHeaders(body, headers);
        boolean isStoreOrder = false;
        if (body.getEnterpriseCode().equalsIgnoreCase("NIKEEUROPE")) {
            Predicate prStoreOrder = exchange.getIn().getHeader("isStoreOrder", Predicate.class);
            exchange.getIn().setHeaders(headers); // set headers for evaluating prStoreOrder expression
            isStoreOrder = prStoreOrder.matches(exchange);
            log.info("prStoreOrder match={}", isStoreOrder);
        }
        setHeadersForServiceCalls(body, headers, isStoreOrder);
        exchange.getIn().setHeaders(headers);
    }

    private void setReferenceHeaders(Order order, Map<String, Object> headers) {
        log.info("Setting reference headers");
        Order.References references = order.getReferences();
        if (references != null && references.getReferences() != null) {
            for (Order.References.Reference reference : references.getReferences()) {
                String name = reference.getName();
                if ("AppId".equalsIgnoreCase(name)) {
                    headers.put("appId", reference.getValue());
                } else if ("TaxRemittedFlag".equalsIgnoreCase(name)) {
                    headers.put("taxRemittedFlag", reference.getValue());
                } else if ("Channel".equalsIgnoreCase(name)) {
                    headers.put("channel", reference.getValue());
                } else if ("G7SIdentifier".equalsIgnoreCase(name)) {
                    headers.put("G7SIdentifier", reference.getValue());
                }
            }
        }
    }

    private void setHeadersForServiceCalls(Order order, Map<String, Object> headers, boolean isStoreOrder) {
        log.info("Setting headers for downstream service calls PII and TCC");
        List<Order.OrderLines.OrderLine> orderLines = order.getOrderLines().getOrderLine();
        List<Order.HeaderCharges.HeaderCharge> headerCharges = order.getHeaderCharges().getHeaderCharges();
        Set<String> addressIdSet = new HashSet<>();
        Set<String> upcCodeSet = new HashSet<>();

        for (Order.OrderLines.OrderLine orderLine : orderLines) {
            if (orderLine.getPersonInfoMarkFor() != null && StringUtils.isNotBlank(orderLine.getPersonInfoMarkFor().getAddressID())) {
                log.info("Adding addressId for PII service call for personInfoMarkFor, lineType={}, orderType={}, enterpriseCode={}",
                        orderLine.getLineType(), headers.get("OrderType"), headers.get("EnterpriseCode"));
                addressIdSet.add(orderLine.getPersonInfoMarkFor().getAddressID());
            }
            if (orderLine.getPersonInfoShipTo() != null && StringUtils.isNotBlank(orderLine.getPersonInfoShipTo().getAddressID())) {
                log.info("Adding addressId for PII service call for personInfoShipTo, lineType={}, orderType={}, enterpriseCode={}",
                        orderLine.getLineType(), headers.get("OrderType"), headers.get("EnterpriseCode"));
                addressIdSet.add(orderLine.getPersonInfoShipTo().getAddressID());
            }
            if (orderLine.getItem() != null && StringUtils.isNotBlank(orderLine.getItem().getUpcCode())) {
                log.info("Adding upcCode for TCC service call, lineType={}, upcCode={}, orderType={}, enterpriseCode={}",
                        orderLine.getLineType(), orderLine.getItem().getUpcCode(), headers.get("OrderType"), headers.get("EnterpriseCode"));
                upcCodeSet.add(orderLine.getItem().getUpcCode());
            }

            //GOLOM-6151:Logic to add Tcc code for shipping line. Refer to XSLT NIKEEU STORE & STANDARD Orders-
            // OneSourceRequestForStore.xsl and TaxManager-To-OneSource.xsl
            //For NIKEUROPE and NIKEUS
            if ((order.getEnterpriseCode().equalsIgnoreCase("NIKEEUROPE") || order.getEnterpriseCode().equalsIgnoreCase("NIKEUS"))
                    && Objects.nonNull(headerCharges) && !orderLine.getOrderedQty().equals("0.00")
                    && Objects.nonNull(order.getExtn()) && Objects.nonNull(orderLine.getLineExtn())
                    && StringUtils.isNotBlank(orderLine.getLineExtn().getExtnShipGroup())) {
                log.info("Adding GTIN for shipping line, isStoreOrder={}", isStoreOrder);
                boolean isStoreOrderEligible = (isStoreOrder && ((StringUtils.isNotBlank(order.getExtn().getExtnComputeHeaderTax())
                        && order.getExtn().getExtnComputeHeaderTax().equalsIgnoreCase("Y"))
                        || (StringUtils.isNotBlank(order.getIsNewOrder()) && order.getIsNewOrder().equalsIgnoreCase("N")
                        && StringUtils.isNotBlank(order.getDraftOrderFlag()) && order.getDraftOrderFlag().equalsIgnoreCase("N"))));
                log.info("Adding GTIN for shipping line, isStoreOrderEligible={}", isStoreOrderEligible);
                for (Order.HeaderCharges.HeaderCharge headerCharge : headerCharges) {
                    if ((StringUtils.isNotBlank(headerCharge.getReference())
                            && headerCharge.getReference().equalsIgnoreCase(orderLine.getLineExtn().getExtnShipGroup()))
                            && (!isStoreOrder || isStoreOrderEligible)) {
                        log.info("Adding GTIN for shipping line, tscGtin={}", tscGtin);
                        upcCodeSet.add(tscGtin); //GTIN for shipping line-
                        headers.put("extnShipGroup", orderLine.getLineExtn().getExtnShipGroup());
                    }
                }
            } // End
        }
        if (!(addressIdSet.isEmpty())) {
            final String nodeIds = String.join(",", addressIdSet);
            if (!(nodeIds.isEmpty())) {
                log.info("AddressID's set for PII service call={}", nodeIds);
                headers.put("piiAddressIdSet", nodeIds);
            }
        }
        if (!(upcCodeSet.isEmpty())) {
            // Logic to split the UPC's in chunk of 25 for each TCC request header
            ArrayList<String> upcCodes = new ArrayList<String>();
            Collection<List<String>> upcChunkList = splitUPCCodes(upcCodeSet, UPC_SPLIT_SIZE);
            upcChunkList.forEach(eachChunk -> {
                upcCodes.add(String.join(",", eachChunk).replace("[", "").replace("]", ""));
            });

            if (!(upcCodes.isEmpty())) {
                log.info("UPCCodes set for TCC service call={}", upcCodes);
                headers.put("tccUPCCodeList", upcCodes);
            }
        }
    }

    /**
     * Method to create the chunk of UPCs
     * @param upcCodeSet
     * @param chunkSize
     * @param <T>
     * @return upcGroupList
     */
    public static <T> Collection<List<T>> splitUPCCodes(Set<T> upcCodeSet, int chunkSize) {
        final AtomicInteger counter = new AtomicInteger();
        final Collection<List<T>> upcGroupList = upcCodeSet.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
                .values();
        return upcGroupList;
    }
}

