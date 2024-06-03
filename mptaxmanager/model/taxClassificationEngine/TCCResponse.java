package com.nike.mptaxmanager.model.taxClassificationEngine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class TCCResponse {
    @JsonProperty("statusCode")
    private String statusCode;
    @JsonProperty("classificationCodes")
    private List<ClassificationCode> classificationCodes;
    @JsonProperty("errors")
    private List<Error> errors;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassificationCode {
        @JsonProperty("gtin")
        private String gtin;
        @JsonProperty("sku")
        private String sku;
        @JsonProperty("classification")
        private String classification;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Error {
        @JsonProperty("requested")
        private String requested;
        @JsonProperty("httpStatus")
        private Integer httpStatus;
        @JsonProperty("message")
        private String message;
    }

}


