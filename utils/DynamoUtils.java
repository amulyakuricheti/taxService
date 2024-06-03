package com.nike.invoiceshipmentwrkr.utils;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nike.invoiceshipmentwrkr.model.ShipmentConsolidated;
import com.nike.invoiceshipmentwrkr.model.bopis.Consolidator;
import com.nike.invoiceshipmentwrkr.model.dynamo.Invoice;
import com.nike.invoiceshipmentwrkr.model.dynamo.Shipment;
import com.nike.invoiceshipmentwrkr.model.dynamo.ShippingCharge;
import com.nike.invoiceshipmentwrkr.model.dynamo.Tax;
import com.nike.invoiceshipmentwrkr.model.dynamo.InvoiceShipmentTaxDetails;
import com.nike.invoiceshipmentwrkr.model.invoicecreation.InvoiceCreation;
import com.nike.invoiceshipmentwrkr.model.mptaxmanager.request.FinalSalesAndTaxesRequest;
import com.nike.invoiceshipmentwrkr.model.mptaxmanager.response.FinalSalesAndUseTaxesResponse;
import com.nike.invoiceshipmentwrkr.model.pulse.PulseEventMessageModel;
import com.nike.invoiceshipmentwrkr.model.taxevent.TaxDetailEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class DynamoUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public void saveShipmentXML(Exchange exchange) {
        log.info("Control in DynamoUtils - saveShipmentXML");
        Shipment shipment = Shipment.builder()
                .orderNo(exchange.getProperty("OrderNumber", String.class))
                .shipmentNo(exchange.getProperty("ShipmentNo", String.class))
                .shipmentXml(exchange.getIn().getBody(String.class))
                .build();

        dynamoDBMapper.save(shipment);
    }

    public void saveTaxServiceRequest(Exchange exchange) {
        log.info("Control in DynamoUtils - saveTaxServiceRequest");

        FinalSalesAndTaxesRequest taxesRequest = exchange.getIn().getBody(FinalSalesAndTaxesRequest.class);
        Tax tax = Tax.builder()
                .orderNo(exchange.getProperty("OrderNumber", String.class))
                .shipmentNo(exchange.getProperty("ShipmentNo", String.class))
                .build();

        try {
            tax.setTaxServiceRequest(mapper.writeValueAsString(taxesRequest));
        } catch (JsonProcessingException e) {
            log.error("Error while converting Tax Request to JSON", e);
        }
        dynamoDBMapper.save(tax);
    }

    public void saveTaxServiceResponse(Exchange exchange) {
        log.info("Control in saveOrderInDynamo - saveTaxServiceResponse");

        ShipmentConsolidated shipmentConsolidated = exchange.getIn().getBody(ShipmentConsolidated.class);
        FinalSalesAndUseTaxesResponse taxResponse = shipmentConsolidated.getTaxesResponse();

        Tax tax = Tax.builder()
                .orderNo(exchange.getProperty("OrderNumber", String.class))
                .shipmentNo(exchange.getProperty("ShipmentNo", String.class))
                .build();
        try {
            tax.setTaxServiceResponse(mapper.writeValueAsString(taxResponse));
        } catch (JsonProcessingException e) {
            log.error("Error while converting Tax Response to JSON", e);
        }
        dynamoDBMapper.save(tax);
        log.info("Successfully Saved in TAX table");
    }

    public void saveCloudOrderInvoice(Exchange exchange) {
        log.info("Control in saveCloudOrderInvoice");

        InvoiceCreation invoiceCreation = exchange.getIn().getBody(InvoiceCreation.class);

        Invoice invoice = Invoice.builder()
                .orderNo(exchange.getProperty("OrderNumber", String.class))
                .shipmentNo(exchange.getProperty("ShipmentNo", String.class))
                .build();
        try {
            invoice.setCloudInvoicePayLoad(mapper.writeValueAsString(invoiceCreation));
        } catch (JsonProcessingException e) {
            log.error("Error while converting InvoceCreation Payload to JSON", e);
        }
        dynamoDBMapper.save(invoice);
    }

    public void saveDomsXml(Exchange exchange) {
        log.info("Control in saveDomsXml");

        String domsXml = exchange.getIn().getBody(String.class);
        Invoice invoice = Invoice.builder()
                .orderNo(exchange.getProperty("OrderNumber", String.class))
                .shipmentNo(exchange.getProperty("ShipmentNo", String.class))
                .domsXML(domsXml)
                .build();
        dynamoDBMapper.save(invoice);
    }

    public void saveShippingChargeReference(Exchange exchange) {
        log.info("Control in DynamoUtils - saveShippingChargeReference");
        ShippingCharge shippingCharge = ShippingCharge.builder()
                .orderNumber(exchange.getProperty("OrderNumber", String.class))
                .shippingChargeReference(exchange.getProperty("TransactionId", String.class))
                .build();
        dynamoDBMapper.save(shippingCharge);
    }

    public ShippingCharge queryShippingCharge(String orderNumber) throws Exception {
        val eav = new HashMap<String, AttributeValue>();
        eav.put(":v1", new AttributeValue().withS(orderNumber));
        log.info("event = Initiating fetching dynamo record with id as orderNumber={}", orderNumber);
        val queryExpression = new DynamoDBQueryExpression<ShippingCharge>()
                .withKeyConditionExpression("orderNumber = :v1")
                .withExpressionAttributeValues(eav)
                .withConsistentRead(true)
                .withLimit(1);

        ShippingCharge shippingCharge = null;
        try {
            shippingCharge = dynamoDBMapper.query(ShippingCharge.class, queryExpression)
                    .stream()
                    .findFirst()
                    .get();
        } catch (Exception NoSuchElementException) {
            return null;
        }

        return shippingCharge;
    }

    /**
     * Method to fetch Shipping Details by PO Number
     *
     * @param purchaseOrderNumber
     * @return InvoiceShipmentTaxDetails object
     * @throws Exception
     */
    public Optional<InvoiceShipmentTaxDetails> queryShippingDetail(String purchaseOrderNumber) throws Exception {
        val eav = new HashMap<String, AttributeValue>();
        eav.put(":v1", new AttributeValue().withS(purchaseOrderNumber));
        log.info("event = Initiating fetching dynamo record with PONumber={}", purchaseOrderNumber);
        val queryExpression = new DynamoDBQueryExpression<InvoiceShipmentTaxDetails>()
                .withKeyConditionExpression("purchaseOrderNumber = :v1")
                .withExpressionAttributeValues(eav)
                .withConsistentRead(true)
                .withLimit(1);

        Optional<InvoiceShipmentTaxDetails> optionalShippingTaxDetail;
        try {
            optionalShippingTaxDetail = dynamoDBMapper.query(InvoiceShipmentTaxDetails.class, queryExpression)
                    .stream()
                    .findFirst();

        } catch (Exception NoSuchElementException) {
            log.error("Record does not exists for the PONumber={} ", purchaseOrderNumber);
            optionalShippingTaxDetail = Optional.empty();
        }

        return optionalShippingTaxDetail;
    }

    /**
     * Method saves tax Event Detail into Dynamo DB with taxFlag = 1
     *
     * @param exchange
     */
    public void saveTaxDetails(Exchange exchange) throws Exception {
        log.info("Control in DynamoUtils - saveTaxDetails");

        TaxDetailEvent taxDetailEvent = exchange.getIn().getBody(TaxDetailEvent.class);
        InvoiceShipmentTaxDetails invoiceShipmentTaxDetails;
        try {
            Optional<InvoiceShipmentTaxDetails> optionalShipmentTaxDetails = queryShippingDetail(taxDetailEvent.getInvoices().getPurchaseOrderNumber());
            log.info("Flipkart/Myntra shipmenttaxdetails request PONumber- {}", taxDetailEvent.getInvoices().getPurchaseOrderNumber());
            if (optionalShipmentTaxDetails.isPresent()) {
                invoiceShipmentTaxDetails = optionalShipmentTaxDetails.get();
                invoiceShipmentTaxDetails.setTaxFlag(1);
                invoiceShipmentTaxDetails.setTaxPayload(mapper.writeValueAsString(taxDetailEvent));
                log.info("Shipment Detail is present for Flipkart/Myntra PONumber - {}", invoiceShipmentTaxDetails.getPurchaseOrderNumber());
                log.info("Tax Detail is updated for Flipkart/Myntra PONumber - {}", invoiceShipmentTaxDetails.getPurchaseOrderNumber());
                if (invoiceShipmentTaxDetails.getShipmentFlag() != null && invoiceShipmentTaxDetails.getShipmentFlag() == 1 && invoiceShipmentTaxDetails.getInvoiceFlag() == 0) {
                    exchange.setProperty("isEligibleForPacPost", true);
                    exchange.getIn().setBody(invoiceShipmentTaxDetails, InvoiceShipmentTaxDetails.class);
                } else {
                    exchange.setProperty("isEligibleForPacPost", false);
                }
            } else {
                invoiceShipmentTaxDetails = InvoiceShipmentTaxDetails.builder()
                        .purchaseOrderNumber(taxDetailEvent.getEventControl().getBusinessKeyValue())
                        .orderNumber(taxDetailEvent.getInvoices().getOrderNumber())
                        .taxFlag(1)
                        .taxPayload(mapper.writeValueAsString(taxDetailEvent))
                        .build();
                log.info("Shipment Detail is not present for Flipkart/Myntra PONumber - {}", taxDetailEvent.getEventControl().getBusinessKeyValue());
                log.info("Tax Detail is created for Flipkart/Myntra PONumber - {}",  taxDetailEvent.getEventControl().getBusinessKeyValue());
            }
            dynamoDBMapper.save(invoiceShipmentTaxDetails);
            log.info("saveTaxDetails - successfully saved tax details for Flipkart/Myntra");

        } catch (JsonProcessingException e) {
            log.error("Error while converting taxDetailEvent to JSON", e);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Method saves PO Number into Dynamo DB with shipmentFlag = 1
     *
     * @param exchange
     */
    public void saveShipmentDetails(Exchange exchange) throws Exception {
        log.info("Control in DynamoUtils - saveShipmentDetails");
        InvoiceShipmentTaxDetails invoiceShipmentTaxDetails;
        PulseEventMessageModel pulseEventMessageModel = exchange.getIn().getBody(PulseEventMessageModel.class);
        log.info("Flipkart/Myntra pulse event message body - {}", exchange.getIn().getBody(String.class));
        log.info("Flipkart/Myntra pulse event message message - {}", pulseEventMessageModel);
        String poNumber = pulseEventMessageModel.getEventContext().getFilterMap().getPoNumber();
        log.info("Flipkart/Myntra Shipment Details for Purchase Order Number = {} will be saved in Dynamodb", poNumber);
        String salesOrderNumber = pulseEventMessageModel.getEventContext().getFilterMap().getOrderNo();
        log.info("Flipkart/Myntra PONumber :: {} and OrderNumber :: {}", poNumber, salesOrderNumber);
        try {
            Optional<InvoiceShipmentTaxDetails> optionalShipmentTaxDetails = queryShippingDetail(poNumber);
            if (optionalShipmentTaxDetails.isPresent()) {
                invoiceShipmentTaxDetails = optionalShipmentTaxDetails.get();
                invoiceShipmentTaxDetails.setShipmentFlag(1);
                invoiceShipmentTaxDetails.setShipmentTime(OffsetDateTime.now().toString());
                invoiceShipmentTaxDetails.setInvoiceFlag(0);
                log.info("Tax Detail is present for Flipkart/Myntra PONumber - {}", invoiceShipmentTaxDetails.getPurchaseOrderNumber());
                log.info("Shipment Detail is updated for Flipkart/Myntra PONumber - {}", invoiceShipmentTaxDetails.getPurchaseOrderNumber());
                if (invoiceShipmentTaxDetails.getTaxFlag() != null && invoiceShipmentTaxDetails.getTaxFlag() == 1) {
                    exchange.setProperty("isEligibleForPacPost", true);
                    exchange.getIn().setBody(invoiceShipmentTaxDetails, InvoiceShipmentTaxDetails.class);
                } else {
                    exchange.setProperty("isEligibleForPacPost", false);
                }
            } else {
                invoiceShipmentTaxDetails = InvoiceShipmentTaxDetails.builder()
                        .purchaseOrderNumber(poNumber)
                        .orderNumber(salesOrderNumber)
                        .shipmentFlag(1)
                        .shipmentTime(OffsetDateTime.now().toString())
                        .invoiceFlag(0)
                        .build();
                log.info("Tax Detail is not present for Flipkart/Myntra PONumber - {}", poNumber);
                log.info("Shipment Detail is created for Flipkart/Myntra PONumber - {}",  poNumber);
            }
            dynamoDBMapper.save(invoiceShipmentTaxDetails);
            log.info("saveShipmentDetails - successfully saved shipment details for Flipkart/Myntra PONumber - {}", poNumber);

        } catch (Exception exe) {
            log.error("Exception occured while saving the shipment details to dynamo DB " + exe);
            exe.printStackTrace();
            throw exe;
        }
    }

    public void updateInvoiceFlag(Exchange exchange) {
        log.info("Control in DynamoUtils - updateInvoiceFlag");
        String poNumber = exchange.getProperty("PONumber", String.class);
        try {
            Optional<InvoiceShipmentTaxDetails> optionalShipmentTaxDetails = queryShippingDetail(poNumber);
            if (optionalShipmentTaxDetails.isPresent()) {
                InvoiceShipmentTaxDetails  invoiceDetail = optionalShipmentTaxDetails.get();
                invoiceDetail.setInvoiceFlag(1);
                dynamoDBMapper.save(invoiceDetail);
                log.info("Invoice Flag successfully updated for Flipkart/Myntra PONumber - {}", poNumber);
            } else {
                log.info("Record does not exists for Flipkart/Myntra PONumber - {}", poNumber);
            }
        } catch (Exception e) {
            log.error("Exception occured while updating invoice flag " + e);
            e.printStackTrace();
        }
    }

    public void saveTaxServiceRequestBopis(Exchange exchange) {
        log.info("Control in DynamoUtils - saveTaxServiceRequestBopis");

        FinalSalesAndTaxesRequest taxesRequest = exchange.getIn().getBody(FinalSalesAndTaxesRequest.class);
        Tax tax = Tax.builder()
                .orderNo(exchange.getProperty("OrderNumber", String.class))
                .shipmentNo(exchange.getProperty("shipAdviceNumber", String.class))
                .build();

        try {
            tax.setTaxServiceRequest(mapper.writeValueAsString(taxesRequest));
        } catch (JsonProcessingException e) {
            log.error("Error while converting Tax Request to JSON", e);
        }
        log.info("saveTaxServiceRequest-before saving in dynamo tax={}", tax);
        dynamoDBMapper.save(tax);
    }

    public void saveTaxServiceResponseBopis(Exchange exchange) {
        log.info("Control in saveOrderInDynamo - saveTaxServiceResponseBopis");
        Consolidator consolidator = exchange.getIn().getBody(Consolidator.class);
        FinalSalesAndUseTaxesResponse taxResponse = consolidator.getTaxesResponse();
        String orderNo = exchange.getProperty("OrderNumber", String.class);
        String shipAdviceNumber = exchange.getProperty("shipAdviceNumber", String.class);
        Tax tax = Tax.builder()
                .orderNo(orderNo)
                .shipmentNo(shipAdviceNumber)
                .build();
        try {
            String taxResponseString = mapper.writeValueAsString(taxResponse);
            log.info("Building Tax object in saveTaxServiceResponseBopis orderNo={},shipmentNo={},taxResponse={} ", orderNo, shipAdviceNumber, taxResponseString);
            tax.setTaxServiceResponse(taxResponseString);
        } catch (JsonProcessingException e) {
            log.error("Error while converting Tax Response to JSON", e);
        }
        log.info("saveTaxServiceResponseBopis-before saving in dynamo tax={}", tax);
        dynamoDBMapper.save(tax);
    }

    public void saveCloudOrderInvoiceBopis(Exchange exchange) {
        log.info("Control in saveCloudOrderInvoiceBopis");

        InvoiceCreation invoiceCreation = exchange.getIn().getBody(InvoiceCreation.class);

        Invoice invoice = Invoice.builder()
                .orderNo(exchange.getProperty("OrderNumber", String.class))
                .shipmentNo(exchange.getProperty("shipAdviceNumber", String.class))
                .build();
        try {
            invoice.setCloudInvoicePayLoad(mapper.writeValueAsString(invoiceCreation));
        } catch (JsonProcessingException e) {
            log.error("Error while converting InvoiceCreation Payload to JSON", e);
        }
        log.info("saveCloudOrderInvoiceBopis- before saving in dynamo invoice={}", invoice);
        dynamoDBMapper.save(invoice);
    }

    public void saveDomsXmlBopis(Exchange exchange) {
        log.info("Control in saveDomsXmlBopis");
        String shipAdviceNumber = exchange.getProperty("shipAdviceNumber", String.class);
        log.info("shipAdviceNumber::saveDomsXmlBopis={}", shipAdviceNumber);
        String domsXml = exchange.getIn().getBody(String.class);
        Invoice invoice = Invoice.builder()
                .orderNo(exchange.getProperty("OrderNumber", String.class))
                .shipmentNo(shipAdviceNumber)
                .domsXML(domsXml)
                .build();
        log.info("saveDomsXmlBopis- before saving in dynamo invoice={}", invoice);
        dynamoDBMapper.save(invoice);
    }

}
