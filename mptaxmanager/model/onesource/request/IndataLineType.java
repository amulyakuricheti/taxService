//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.11.30 at 10:27:01 AM PST 
//


package com.nike.mptaxmanager.model.onesource.request;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class IndataLineType {
    @XmlElement(name = "EXTN_COMPANY_CODE")
    protected String extncompanycode;
    @XmlElement(name = "COMMODITY_CODE")
    protected String commoditycode;
    @XmlElement(name = "PRODUCT_CODE")
    protected String productcode;
    @XmlElement(name = "DESCRIPTION")
    protected String description;
    @XmlElement(name = "GROSS_AMOUNT")
    protected String grossamount;
    @XmlElement(name = "GROSS_PLUS_TAX")
    protected String grossplustax;
    @XmlElement(name = "LINE_NUMBER")
    protected BigDecimal linenumber;
    @XmlElement(name = "LOCATION")
    protected LocationNameType location;
    @XmlElement(name = "QUANTITIES")
    protected QuantitiesType quantities;
    @XmlElement(name = "QUANTITY")
    protected BigInteger quantity;
    @XmlElement(name = "SHIP_TO")
    protected CommonAddressType shipto;
    @XmlElement(name = "SHIP_FROM")
    protected CommonAddressType shipfrom;
    @XmlElement(name = "INVOICE_DATE")
    protected String invoicedate;
    @XmlElement(name = "USER_ELEMENT")
    protected List<UserElementType> userelement;
    @XmlAttribute(name = "ID")
    protected String id;
}
