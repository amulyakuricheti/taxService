package com.nike.mptaxmanager.processor;

import com.nike.mptaxmanager.configuration.JaxbSingletonInstance;
import com.nike.mptaxmanager.exception.TCCCodeMappingException;
import com.nike.mptaxmanager.model.onesource.response.Outdata;
import com.nike.mptaxmanager.model.onesource.response.OutdataInvoiceType;
import com.nike.mptaxmanager.model.onesource.response.OutdataLineType;
import com.nike.mptaxmanager.model.onesource.response.OutdataTaxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Objects;
/**
 * Copyright © 2021 Nike. All rights reserved.
 *
 * @Created by Sumant on 27/9/2021
 * Project Name : MPTaxManagerService
 * File Name : TaxClassificationCodeMappingProcessor
 * Description : This class is to read the tcc code from exchange property and bind it in taxManager Response
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TaxClassificationCodeMappingProcessor implements Processor {

    private static final String CLASSIFICATION_CODE = "classificationCode";

    @Value("${tcc.shipping.charge.gtin:00000000000006}")
    private String tscGtin;

    /**
     * This Processor is for reading the classificationCode exchange properity for EU orders and map in to construct final taxManager response.
     *
     * @param exchange
     */
    @Override
    public void process(Exchange exchange) {
        log.info("Tax Classification Code mapper to add the tcc code in taxManager response");
        String order = exchange.getIn().getBody(String.class);
        String orderNumber = exchange.getIn().getHeader("orderNumber", String.class);
        try {
            Unmarshaller jaxbUnmarshaller = JaxbSingletonInstance.getInstance().createUnmarshaller();
            log.info("Before unmarshalling the oneSource response in Outdata object");
            Outdata outData = (Outdata) jaxbUnmarshaller.unmarshal(new StringReader(order));
            log.info("TaxClassificationCodeMappingProcessor : order unmarshalling completed.");
            mapClassificationCode(outData, exchange);
            exchange.getIn().setBody(marshalOutDataObject(outData));
        } catch (Exception ex) {
            log.error("Exception occurred in TaxClassificationCodeMappingProcessor while maping the tcc in oneSourceResponse, orderNumber={}, exception={}", orderNumber, ex);
            throw new TCCCodeMappingException("Exception occurred in mapping the classification in oneSourceResponse,. Error={}", ex);
        }
    }

    /**
     * method to map ClassificationCode in Outdata.
     *
     * @param outData
     * @param exchange
     */
    private void mapClassificationCode(Outdata outData, Exchange exchange) {
        try {
            String extnShipGroup = exchange.getIn().getHeader("extnShipGroup", String.class);
            log.info("Mapped tcc property details={}, extnShipGroup={}", exchange.getProperty(CLASSIFICATION_CODE), extnShipGroup);
            HashMap<String, String> classificationCodeMap = (HashMap<String, String>) exchange.getProperty(CLASSIFICATION_CODE);
            if ((classificationCodeMap != null) && (outData != null)) {
                for (OutdataInvoiceType invoice : outData.getInvoice()) {
                    for (OutdataLineType invoiceLine : invoice.getLine()) {
                        for (OutdataTaxType tax : invoiceLine.getTax()) {
                            String erpTaxCode = tax.getErptaxcode();
                            String tccCode = null;
                            String invoiceId = invoiceLine.getId().split("-")[0];
                            tccCode = classificationCodeMap.get(invoiceId);
                            if (erpTaxCode != null && tccCode != null) {
                                tax.setErptaxcode(erpTaxCode.concat("-").concat(tccCode));
                                log.info("TCC-TRCC Code for Orderlinekey:{} is {}", invoiceId, tax.getErptaxcode());
                            } else if (StringUtils.isNotBlank(erpTaxCode)
                                    && StringUtils.isNotBlank(extnShipGroup)
                                    && extnShipGroup.equalsIgnoreCase(invoiceLine.getId())
                                    && Objects.nonNull(classificationCodeMap.get(tscGtin))) {
                                tax.setErptaxcode(erpTaxCode.concat("-").concat(classificationCodeMap.get(tscGtin)));
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Exception occurred in mapClassificationCode of TaxClassificationCodeMappingProcessor,  exception={}", ex);
            throw new TCCCodeMappingException("Exception occurred in mapClassificationCode of TaxClassificationCodeMappingProcessor. Error={}", ex);
        }
    }

    /**
     * marshalOutDataObject to marshal Outdata to xml object.
     *
     * @param obj
     * @return
     * @throws Exception
     */
    private String marshalOutDataObject(Outdata obj) throws Exception {
        StringWriter sw = new StringWriter();
        try {
            Marshaller jaxbMarshaller = JaxbSingletonInstance.getInstance().createMarshaller();
            jaxbMarshaller.marshal(obj, sw);
        } finally {
            sw.close();
        }
        return sw.toString();
    }
}

