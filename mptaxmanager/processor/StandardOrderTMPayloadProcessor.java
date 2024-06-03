package com.nike.mptaxmanager.processor;

import com.nike.internal.util.StringUtils;
import com.nike.mptaxmanager.model.payload.Order;
import com.nike.mptaxmanager.utils.CompanyCode;
import com.nike.mptaxmanager.utils.DefaultShipFromAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * <p>Input: {@link Order} <i>(Object)</i></p>
 * <p>Output: {@link Order} <i>(Object)</i></p>
 */
@Slf4j
public class StandardOrderTMPayloadProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        Order payload = exchange.getIn().getBody(Order.class);
        final String orderNumber = payload.getOrderNo();
        final String enterpriseCode = payload.getEnterpriseCode();
        setShipFromAddress(payload);
        setShipToAddressIfEgc(payload);

        if (!enterpriseCode.equalsIgnoreCase("NIKEUS")) {
            setCompanyCode(payload);
            setLineCompanyCode(payload);
        }
        setOrderInvoices(payload, orderNumber);
        exchange.getIn().setBody(payload);
        log.info("Setting additional attributes to StandardOrder TMPayload request={} for orderNumber={}", payload, orderNumber);

    }

    private static void setOrderInvoices(Order payload, final String orderNumber) {
        if (payload.getOrderInvoiceList() == null) {
            log.error("StandardOrderTMPayloadProcessor:Found INVALID OrderInvoiceList for orderNumber={}, enterpriseCode={}, orderType={}",
                    orderNumber, payload.getEnterpriseCode(), payload.getOrderType());
            return;
        }
        List<Order.OrderInvoiceList.InvoiceDetail> invoiceDetails = payload.getOrderInvoiceList().getInvoiceDetail();
        if (invoiceDetails != null) {
            List<Order.OrderInvoiceList.OrderInvoice> orderInvoices = new ArrayList<>();
            for (Order.OrderInvoiceList.InvoiceDetail invoiceDetail : invoiceDetails) {
                final Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader invoiceHeader = invoiceDetail.getInvoiceHeader();
                if (invoiceHeader != null && invoiceHeader.getHeaderCharges() != null && invoiceHeader.getHeaderCharges().getHeaderCharge() != null) {
                    boolean isFound = invoiceHeader.getHeaderCharges().getHeaderCharge().stream()
                            .anyMatch(h -> "Shipping".equalsIgnoreCase(h.getChargeCategory()) && h.getChargeAmount() != null && Double.valueOf(h.getChargeAmount()) > 0);
                    if (isFound) {
                        Order.OrderInvoiceList.OrderInvoice orderInvoice = null;
                        Order.OrderInvoiceList.InvoiceDetail.Shipments.Shipment.ShipNode.ShipNodePersonInfo nodePersonInfo = null;
                        if (invoiceDetail.getShipments() != null && invoiceDetail.getShipments().getShipment() != null
                                && invoiceDetail.getShipments().getShipment().get(0) != null
                                && invoiceDetail.getShipments().getShipment().get(0).getShipNode() != null) {
                            nodePersonInfo = invoiceDetail.getShipments().getShipment().get(0).getShipNode().getShipNodePersonInfo();
                        }
                        if ("SHIPMENT".equalsIgnoreCase(invoiceHeader.getInvoiceType())) {
                            Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo shipFromForShippingCharge = null;
                            if (invoiceHeader.getShipment() != null) {
                                final Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode shipNode = invoiceHeader.getShipment().getShipNode();
                                if (shipNode != null) {
                                    final Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo shipNodePersonInfo = shipNode.getShipNodePersonInfo();
                                    orderInvoice = new Order.OrderInvoiceList.OrderInvoice();
                                    orderInvoice.setNShipNode(shipNode.getShipnodeKey());
                                    if (shipNodePersonInfo != null) {
                                        shipFromForShippingCharge = new Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo();
                                        shipFromForShippingCharge.setCity(shipNodePersonInfo.getCity());
                                        shipFromForShippingCharge.setCountry(shipNodePersonInfo.getCountry());
                                        shipFromForShippingCharge.setState(shipNodePersonInfo.getState());
                                        shipFromForShippingCharge.setZipCode(shipNodePersonInfo.getZipCode());
                                        orderInvoice.setShipFromForShippingCharge(shipFromForShippingCharge);
                                    } else {
                                        log.error("StandardOrderTMPayloadProcessor:Found INVALID ShipNodePersonInfo for orderNumber={}, enterpriseCode={}, orderType={}",
                                            orderNumber, payload.getEnterpriseCode(), payload.getOrderType());
                                    }
                                } else {
                                    log.error("StandardOrderTMPayloadProcessor:Found INVALID Shipnode for orderNumber={}, enterpriseCode={}, orderType={}",
                                        orderNumber, payload.getEnterpriseCode(), payload.getOrderType());
                                }
                            }

                        } else if (invoiceHeader.getExtn() != null
                                && StringUtils.isNotBlank(invoiceHeader.getExtn().getExtnCustomerPONo())
                                && nodePersonInfo != null && StringUtils.isNotBlank(nodePersonInfo.getZipCode())) {
                            Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo shipFromForShippingCharge = null;
                            final Order.OrderInvoiceList.InvoiceDetail.Shipments.Shipment.ShipNode shipNode = invoiceDetail.getShipments().getShipment().get(0).getShipNode();
                            if (shipNode != null) {
                                orderInvoice = new Order.OrderInvoiceList.OrderInvoice();
                                orderInvoice.setNShipNode(shipNode.getShipNode());
                                shipFromForShippingCharge = new Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo();
                                shipFromForShippingCharge.setCity(nodePersonInfo.getCity());
                                shipFromForShippingCharge.setCountry(nodePersonInfo.getCountry());
                                shipFromForShippingCharge.setState(nodePersonInfo.getState());
                                shipFromForShippingCharge.setZipCode(nodePersonInfo.getZipCode());
                                orderInvoice.setShipFromForShippingCharge(shipFromForShippingCharge);
                            } else {
                                log.error("StandardOrderTMPayloadProcessor:Found INVALID Shipnode for orderNumber={}, enterpriseCode={}, orderType={}",
                                    orderNumber, payload.getEnterpriseCode(), payload.getOrderType());
                            }
                        } else {
                            if ("NIKEUS".equalsIgnoreCase(payload.getEnterpriseCode())) {
                                orderInvoice = new Order.OrderInvoiceList.OrderInvoice();
                                orderInvoice.setNShipNode(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_SHIP_NODE);
                                Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo shipFromForShippingCharge = new Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo();
                                shipFromForShippingCharge.setCity(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_CITY);
                                shipFromForShippingCharge.setState(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_STATE);
                                shipFromForShippingCharge.setCountry(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_COUNTRY);
                                shipFromForShippingCharge.setZipCode(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_ZIP_CODE);
                                orderInvoice.setShipFromForShippingCharge(shipFromForShippingCharge);
                            } else {
                                orderInvoice = new Order.OrderInvoiceList.OrderInvoice();
                                orderInvoice.setNShipNode(DefaultShipFromAddress.DEFAULT_SHIP_FROM_EUROPE_SHIP_NODE);
                                Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo shipFromForShippingCharge = new Order.OrderInvoiceList.InvoiceDetail.InvoiceHeader.Shipment.ShipNode.ShipNodePersonInfo();
                                shipFromForShippingCharge.setCity(DefaultShipFromAddress.DEFAULT_SHIP_FROM_EUROPE_CITY);
                                shipFromForShippingCharge.setCountry(DefaultShipFromAddress.DEFAULT_SHIP_FROM_EUROPE_COUNTRY);
                                shipFromForShippingCharge.setZipCode(DefaultShipFromAddress.DEFAULT_SHIP_FROM_EUROPE_ZIP_CODE);
                                orderInvoice.setShipFromForShippingCharge(shipFromForShippingCharge);
                            }
                        }
                        orderInvoices.add(orderInvoice);
                    }
                }
            }
            payload.getOrderInvoiceList().setOrderInvoices(orderInvoices);
            payload.getOrderInvoiceList().setInvoiceDetail(Collections.emptyList());
        }
    }

    private static void setCompanyCode(final Order payload) {
        final String orderNumber = payload.getOrderNo();
        if (payload.getExtn() != null && StringUtils.isNotBlank(payload.getExtn().getExtnLocale())) {
            String localStr = payload.getExtn().getExtnLocale();
            String[] arrayStr = localStr.split("_");
            if (arrayStr != null && arrayStr.length >= 2) {
                String country = arrayStr[1];
                payload.setCompanyCode(CompanyCode.getCompanyCode(country));
            } else {
                log.error("StandardOrderTMPayloadProcessor:Found INVALID ExtnLocale for orderNumber={}, enterpriseCode={}, orderType={}",
                    orderNumber, payload.getEnterpriseCode(), payload.getOrderType());
            }
        }
    }

    private static void setShipFromAddress(final Order payload) {
        if (payload.getOrderLines() != null && payload.getOrderLines().getOrderLine() != null) {
            payload.getOrderLines().getOrderLine().stream()
                    .filter(getOrderLinePredicate())
                    .forEach(setShipFromAddress(payload.getEnterpriseCode()));
        }
    }

    private static void setShipToAddressIfEgc(final Order payload) {
        if (payload.getOrderLines() != null && payload.getOrderLines().getOrderLine() != null) {
            for (Order.OrderLines.OrderLine orderLine : payload.getOrderLines().getOrderLine()) {
                if (isElectronicGiftCardLine(orderLine)) {
                    setDefaultShipToAddress(orderLine);
                }
            }
        }
    }

    private static Consumer<Order.OrderLines.OrderLine> setShipFromAddress(final String enterpriseCode) {
        return o -> {
            Order.OrderLines.OrderLine.ShipFromAddress shipFromAddress = o.getShipFromAddress();
            if (shipFromAddress == null || shipFromAddress.getShipNode() == null || shipFromAddress.getShipNode().isEmpty()) {
                shipFromAddress = new Order.OrderLines.OrderLine.ShipFromAddress();
                setDefaultShipNodeAddress(shipFromAddress, enterpriseCode);
                o.setShipFromAddress(shipFromAddress);
            } else {
                setDefaultShipNodeAddress(shipFromAddress, enterpriseCode);
            }
        };
    }

    private static void setDefaultShipNodeAddress(Order.OrderLines.OrderLine.ShipFromAddress shipFromAddress, final String enterpriseCode) {

        if ("NIKEUS".equalsIgnoreCase(enterpriseCode)) {
            shipFromAddress.setAddressLine1(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_ADDRESS1);
            shipFromAddress.setShipNode(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_SHIP_NODE);
            shipFromAddress.setCity(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_CITY);
            shipFromAddress.setState(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_STATE);
            shipFromAddress.setCountry(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_COUNTRY);
            shipFromAddress.setZipCode(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_ZIP_CODE);
        } else {

            shipFromAddress.setAddressLine1(DefaultShipFromAddress.DEFAULT_SHIP_FROM_EUROPE_ADDRESS1);
            shipFromAddress.setShipNode(DefaultShipFromAddress.DEFAULT_SHIP_FROM_EUROPE_SHIP_NODE);
            shipFromAddress.setCity(DefaultShipFromAddress.DEFAULT_SHIP_FROM_EUROPE_CITY);
            shipFromAddress.setCountry(DefaultShipFromAddress.DEFAULT_SHIP_FROM_EUROPE_COUNTRY);
            shipFromAddress.setZipCode(DefaultShipFromAddress.DEFAULT_SHIP_FROM_EUROPE_ZIP_CODE);
        }
    }

    private static void setDefaultShipToAddress(Order.OrderLines.OrderLine orderLine) {
        Order.OrderLines.OrderLine.PersonInfoShipTo shipToAddress = orderLine.getPersonInfoShipTo();

        shipToAddress.setCity(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_CITY);
        shipToAddress.setState(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_STATE);
        shipToAddress.setCountry(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_COUNTRY);
        shipToAddress.setZipCode(DefaultShipFromAddress.DEFAULT_SHIP_FROM_US_ZIP_CODE);
    }

    private static Predicate<Order.OrderLines.OrderLine> getOrderLinePredicate() {
        return o -> ("NIKEID".equalsIgnoreCase(o.getLineType()))
                || (o.getShipFromAddress() != null && StringUtils.isBlank(o.getShipFromAddress().getZipCode()))
                || (o.getExtn() != null && StringUtils.isNotBlank(o.getExtn().getExtnCommerceItemID()));
    }

    private static boolean isElectronicGiftCardLine(Order.OrderLines.OrderLine orderLine) {
        return "EGC".equalsIgnoreCase(orderLine.getLineType());
    }

    private static void setLineCompanyCode(final Order payload) {
        if (payload.getOrderLines() != null && payload.getOrderLines().getOrderLine() != null) {
            payload.getOrderLines().getOrderLine().forEach(line -> line.setCompanyCode(CompanyCode.getStoreCompanyCode(line.getShipFromAddress().getCountry())));
        }
    }
}

