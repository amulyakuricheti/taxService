package com.nike.mptaxmanager.processor;

import com.nike.internal.util.StringUtils;
import com.nike.mptaxmanager.cache.TaxManagerCache;
import com.nike.mptaxmanager.model.Order;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.stream.Stream;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class ShipNodeProcessor implements Processor {
    @Autowired
    private TaxManagerCache taxManagerCache;

    @Override
    public void process(Exchange exchange) {
        Object orderNumber = exchange.getIn().getHeader("orderNumber");
        log.info("Starting ShipNodeProcessor for orderNumber={}", orderNumber);
        Order body = exchange.getIn().getBody(Order.class);
        List<Order.OrderLines.OrderLine> orderLineList = body.getOrderLines().getOrderLine();
        Set<String> nodeIdsSet = orderLineList.stream().filter(orderLine -> orderLine.getShipments() != null)
                .flatMap(s -> s.getShipments().getShipments() != null ? s.getShipments().getShipments().stream() : Stream.empty())
                .map(shipment -> shipment.getShipNode())
                .filter(node -> node != null && StringUtils.isNotBlank(node.getShipNode()) && null == taxManagerCache.findShipNodeByKey(node.getShipNode()))
                .map(n -> n.getShipNode())
                .collect(Collectors.toSet());
        String nodeIds = null;
        log.info("ShipNodeIdSet={} for orderNumber={}", nodeIdsSet, orderNumber);
        if (nodeIdsSet != null && !nodeIdsSet.isEmpty()) {
            nodeIds = String.join(",", nodeIdsSet);
            exchange.getIn().setHeader("shipNodeIds", nodeIds);
        }
        log.info("Successfully set ShipNodeIds={} for orderNumber={}", nodeIds, orderNumber);
    }
}

