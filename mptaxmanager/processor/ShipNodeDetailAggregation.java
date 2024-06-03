package com.nike.mptaxmanager.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nike.internal.util.StringUtils;
import com.nike.mptaxmanager.cache.TaxManagerCache;
import com.nike.mptaxmanager.exception.ShipNodeException;
import com.nike.mptaxmanager.model.Order;
import com.nike.mptaxmanager.model.ShipNode;
import com.nike.mptaxmanager.model.ShipNodes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Aggregation class to handle request data object and shipNode details data object
 */

@Component
@Slf4j
@AllArgsConstructor
public class ShipNodeDetailAggregation implements AggregationStrategy {
    @Autowired
    private TaxManagerCache taxManagerCache;

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        Boolean shipNodeFlag = true;
        ObjectMapper mapper = new ObjectMapper();
        Order request = oldExchange.getIn().getBody(Order.class);
        String orderNumber = request.getOrderNo();
        log.info("Control in ShipNodeDetailAggregation.aggregate(), orderNumber={}", orderNumber);
        String shipNodeDetailString = newExchange.getIn().getBody(String.class);
        ShipNodes shipNodes;

        if (StringUtils.isBlank(shipNodeDetailString)) {
            shipNodeFlag = false;
            log.info("New Exchange is null as we haven't triggered any ShipNode call");
        }

        if (shipNodeFlag) {
            try {
                shipNodes = mapper.readValue(shipNodeDetailString, ShipNodes.class);
                log.info("Parsed ShipNode detail Response Model, orderNumber={}", orderNumber);
            } catch (Exception e) {
                log.error("Error in parsing the ShipNodeService response={} with error={}", shipNodeDetailString, e);
                throw new ShipNodeException("Invalid ShipNodeService Response when mapping to ShipNodeService model. Error={}", e);
            }

            log.info("start loop for ShipNodeDetailAggregation orderNumber={}", orderNumber);
            if (null != shipNodes && null != shipNodes.getShipNodes() && !shipNodes.getShipNodes().isEmpty()) {
                log.info("inside loop for ShipNodeDetailAggregation orderNumber={}", orderNumber);
                log.info("inside loop for ShipNodeDetailAggregation shipNodes={}", shipNodes);
                final Map<String, ShipNode> shipNodesMap = shipNodes.getShipNodes()
                        .stream()
                        .collect(Collectors.toMap(shipNode -> shipNode.getId(), shipNode -> shipNode, (oldValue, newValue) -> oldValue
                                )
                        );
                //Iterates the shipNodes and sets the shipNode address if address not found
                List<Order.OrderLines.OrderLine> orderLineList = request.getOrderLines().getOrderLine();
                orderLineList.stream().filter(orderLine -> orderLine.getShipments() != null)
                        .flatMap(s -> s.getShipments().getShipments() != null ? s.getShipments().getShipments().stream() : Stream.empty())
                        .map(shipment -> shipment.getShipNode())
                        .filter(node -> node != null && StringUtils.isNotBlank(node.getShipNode()))
                        .forEach(n -> addShipNodeAddress(n, shipNodesMap));
            }
        }

        log.info("start loop for ShipNodeDetailAggregation.CurrentShipmentShipFrom orderNumber={}", orderNumber);
        // set shipNode details for CurrentShipmentShipFrom.ShipNode
        if (request.getCurrentShipmentShipFrom() != null) {
            log.info("inside loop for ShipNodeDetailAggregation.CurrentShipmentShipFrom orderNumber={}", orderNumber);
            Order.CurrentShipmentShipFrom.ShipNode node = request.getCurrentShipmentShipFrom().getShipNode();
            if (node != null) {
                log.info("inside node loop for ShipNodeDetailAggregation.CurrentShipmentShipFrom orderNumber={}", orderNumber);
                final String shipNodeKey = node.getShipnodeKey();
                Order.CurrentShipmentShipFrom.ShipNode.PersonInfo personInfo = node.getShipNodePersonInfo();
                if (null != shipNodeKey && !shipNodeKey.isEmpty()) {
                    ShipNode shipNode = taxManagerCache.findShipNodeByKey(shipNodeKey);
                    if (null != shipNode) {
                        final ShipNode.Address address = shipNode.getAddress();
                        personInfo.setCity(address.getCity());
                        personInfo.setCountry(address.getCountry());
                        personInfo.setState(address.getState());
                        personInfo.setZipCode(address.getZipCode());
                    }
                }
            }
        }
        log.info("before setting to oldExchange ShipNodeDetailAggregation orderNumber={}", orderNumber);
        oldExchange.getIn().setBody(request);
        log.info("end ShipNodeDetailAggregation orderNumber={}", orderNumber);
        return oldExchange;
    }

    /**
     * Sets shipNode address If shipNode contains valid addressId
     *
     * @param shipNodeIn   the shipNode to be set address info
     * @param shipNodesMap contains the map of addressID and shipNode details
     */
    private void addShipNodeAddress(final Order.CurrentShipmentShipFrom.ShipNode shipNodeIn, final Map<String, ShipNode> shipNodesMap) {
        final String shipNodeKey = shipNodeIn.getShipNode();
        if (shipNodeKey != null) {
            taxManagerCache.putShipNodeByKey(shipNodeKey, shipNodesMap.get(shipNodeKey));
        }
        setShipNodeAddress(shipNodeIn);
    }

    /**
     * Sets the shipNode address details for given shipNode
     *
     * @param shipNodeIn contains shipNode address details
     */
    private void setShipNodeAddress(final Order.CurrentShipmentShipFrom.ShipNode shipNodeIn) {
        final String shipNodeKey = shipNodeIn.getShipNode();
        Order.CurrentShipmentShipFrom.ShipNode.PersonInfo personInfo = shipNodeIn.getShipNodePersonInfo();
        ShipNode shipNode = null;
        if (null != shipNodeKey) {
            shipNode = taxManagerCache.findShipNodeByKey(shipNodeKey);
        }
        if (null != shipNode) {
            final ShipNode.Address address = shipNode.getAddress();
            personInfo.setCity(address.getCity());
            personInfo.setCountry(address.getCountry());
            personInfo.setState(address.getState());
            personInfo.setZipCode(address.getZipCode());
        }
    }
}

