package com.nike.mptaxmanager.model.rulesengine;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RulesEngineRequest {

    private String omsRegionReference; // this will be the enterpriseCode in sterling request and omsRegionReference in rulesEngine request
    private String channel;
    private String orderClassification; //this will be the orderType in sterling request
    private String appId;
    private String orderType; // this will be the documentType in sterling request
    private boolean taxRemitted;
    private boolean draftOrder;
    private boolean newOrder;
}

