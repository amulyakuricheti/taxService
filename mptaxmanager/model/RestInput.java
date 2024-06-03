package com.nike.mptaxmanager.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RestInput {

    private String inputType;
    private StringBuffer processlog;

    public void setProcessLog(String processorType) {
        if (processlog == null) {
            processlog = new StringBuffer();
        }
        processlog.append("PROCESSED by processor: " + processorType + " ");
    }

}

