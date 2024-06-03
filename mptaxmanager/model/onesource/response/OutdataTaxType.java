//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.11.30 at 10:27:01 AM PST 
//


package com.nike.mptaxmanager.model.onesource.response;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.util.List;


@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class OutdataTaxType {

    @XmlElement(name = "ADDRESS_TYPE", required = true)
    protected String addresstype;
    @XmlElement(name = "ADMIN_ZONE_LEVEL", required = true)
    protected String adminzonelevel;
    @XmlElement(name = "AUTHORITY_NAME", required = true)
    protected String authorityname;
    @XmlElement(name = "AUTHORITY_TYPE", required = true)
    protected String authoritytype;
    @XmlElement(name = "CALCULATION_METHOD")
    protected String calculationmethod;
    @XmlElement(name = "COMMENT")
    protected String comment;
    @XmlElement(name = "ERP_TAX_CODE")
    protected String erptaxcode;
    @XmlElement(name = "EFFECTIVE_ZONE_LEVEL", required = true)
    protected String effectivezonelevel;
    @XmlElement(name = "INVOICE_DESCRIPTION")
    protected String invoicedescription;
    @XmlElement(name = "JURISDICTION_TEXT", required = true)
    protected String jurisdictiontext;
    @XmlElement(name = "MESSAGE")
    protected Message message;
    @XmlElement(name = "LOCATION_CODE")
    protected String locationcode;
    @XmlElement(name = "RULE_ORDER")
    protected BigDecimal ruleorder;
    @XmlElement(name = "TAXABLE_COUNTRY")
    protected String taxablecountry;
    @XmlElement(name = "TAXABLE_COUNTRY_NAME")
    protected String taxablecountryname;
    @XmlElement(name = "TAXABLE_STATE")
    protected String taxablestate;
    @XmlElement(name = "TAXABLE_COUNTY")
    protected String taxablecounty;
    @XmlElement(name = "TAXABLE_CITY")
    protected String taxablecity;
    @XmlElement(name = "TAXABLE_POSTCODE")
    protected String taxablepostcode;
    @XmlElement(name = "TAX_RATE_CODE")
    protected String taxratecode;
    @XmlElement(name = "TAX_TYPE", required = true)
    protected String taxtype;
    @XmlElement(name = "ZONE_NAME")
    protected String zonename;
    @XmlElement(name = "ZONE_LEVEL", required = true)
    protected String zonelevel;
    @XmlElement(name = "TAX_RATE")
    protected String taxrate;
    @XmlElement(name = "NATURE_OF_TAX")
    protected String natureoftax;
    @XmlElement(name = "EU_TRANSACTION")
    protected Boolean eutransaction;
    @XmlElement(name = "AUTHORITY_UUID")
    protected String authorityuuid;
    @XmlElement(name = "AUTHORITY_CURRENCY_CODE")
    protected String authoritycurrencycode;
    @XmlElement(name = "CURRENCY_CONVERSION")
    protected List<CurrencyConversionType> currencyconversion;
    @XmlElement(name = "EXEMPT_AMOUNT")
    protected ConvertedCurrencyAmountType exemptamount;
    @XmlElement(name = "GROSS_AMOUNT")
    protected ConvertedCurrencyAmountType grossamount;
    @XmlElement(name = "NON_TAXABLE_BASIS")
    protected ConvertedCurrencyAmountType nontaxablebasis;
    @XmlElement(name = "TAXABLE_BASIS")
    protected ConvertedCurrencyAmountType taxablebasis;
    @XmlElement(name = "TAX_AMOUNT")
    protected ConvertedCurrencyAmountType taxamount;
    @XmlElement(name = "TAX_DETERMINATION_DATE", required = true)
    protected String taxdeterminationdate;
    @XmlElement(name = "TAX_POINT_DATE", required = true)
    protected String taxpointdate;
}

