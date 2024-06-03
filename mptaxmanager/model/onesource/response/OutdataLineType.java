//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.11.30 at 10:27:01 AM PST 
//


package com.nike.mptaxmanager.model.onesource.response;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class OutdataLineType {
    @XmlElement(name = "COMMODITY_CODE")
    protected String commoditycode;
    @XmlElement(name = "DESCRIPTION")
    protected String description;
    @XmlElement(name = "GROSS_AMOUNT")
    protected String grossamount;
    @XmlElement(name = "IS_BUSINESS_SUPPLY")
    protected String isbusinesssupply;
    @XmlElement(name = "LINE_NUMBER", required = true)
    protected BigDecimal linenumber;
    @XmlElement(name = "MESSAGE")
    protected List<Message> message;
    @XmlElement(name = "POINT_OF_TITLE_TRANSFER", required = true)
    protected String pointoftitletransfer;
    @XmlElement(name = "SHIP_FROM_COUNTRY")
    protected String shipfromcountry;
    @XmlElement(name = "SHIP_TO_COUNTRY")
    protected String shiptocountry;
    @XmlElement(name = "TOTAL_TAX_AMOUNT", required = true)
    protected String totaltaxamount;
    @XmlElement(name = "TAX_CODE")
    protected String taxcode;
    @XmlElement(name = "TAX")
    protected List<OutdataTaxType> tax;
    @XmlElement(name = "TRANSACTION_TYPE")
    protected String transactiontype;
    @XmlElement(name = "UNIT_OF_MEASURE")
    protected String unitofmeasure;
    @XmlElement(name = "QUANTITIES")
    protected QuantitiesType quantities;
    @XmlElement(name = "INVOICE_DATE")
    protected String invoicedate;
    @XmlElement(name = "TAX_SUMMARY")
    protected OutdataTaxSummaryType taxsummary;
    @XmlAttribute(name = "ID", required = true)
    protected String id;
}

