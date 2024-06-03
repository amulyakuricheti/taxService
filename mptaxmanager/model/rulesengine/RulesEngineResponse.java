package com.nike.mptaxmanager.model.rulesengine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RulesEngineResponse {
    private boolean taxRecalcRequired;
    private String taxEngine;
}

