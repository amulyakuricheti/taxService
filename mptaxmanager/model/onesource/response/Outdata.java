//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.11.30 at 10:27:01 AM PST 
//


package com.nike.mptaxmanager.model.onesource.response;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "OUTDATA")
public class Outdata {

    @XmlElement(name = "REQUEST_STATUS")
    protected OutdataRequestStatusType requeststatus;
    @XmlElement(name = "INVOICE", required = true)
    protected List<OutdataInvoiceType> invoice;
    @XmlAttribute(name = "version")
    protected String version;
}

