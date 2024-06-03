package com.nike.mptaxmanager.model.onesource.response;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class Message {
    @XmlElement(name = "LOCATION")
    protected String location;
    @XmlElement(name = "CATEGORY")
    protected String category;
    @XmlElement(name = "CODE")
    protected String code;
    @XmlElement(name = "MESSAGE_TEXT")
    protected String message_text;
    @XmlElement(name = "SEVERITY")
    protected String severity;
}

