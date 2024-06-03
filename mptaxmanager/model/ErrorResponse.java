package com.nike.mptaxmanager.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Error")
public class ErrorResponse {

    @XmlAttribute(name = "ErrorCode")
    private String errorCode;
    @XmlAttribute(name = "ErrorDescription", required = true)
    private String errorDescription;
    @XmlAttribute(name = "Error")
    private String error;
}

