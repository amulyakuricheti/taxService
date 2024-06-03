package com.nike.mptaxmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemResponse {
    @JsonProperty("gtin")
    private String gtin;
    @JsonProperty("parentType")
    private String parentType;
    @JsonProperty("productId")
    private String productId;
    @JsonProperty("cdbColorDescription")
    private String cdbColorDescription;
    @JsonProperty("nikeSize")
    private String nikeSize;
    @JsonProperty("cdbStyleCode")
    private String cdbStyleCode;
    @JsonProperty("cdbColorCode")
    private String cdbColorCode;
    @JsonProperty("cdbDisplaySize")
    private String cdbDisplaySize;
    @JsonProperty("cdbDivision")
    private String cdbDivision;
    @JsonProperty("styleColor")
    private String styleColor;
    @JsonProperty("styleType")
    private String styleType;
    @JsonProperty("productType")
    private String productType;
    @JsonProperty("commodityCode")
    private String commodityCode;
    @JsonProperty("imageLinks")
    private List<String> imageLinks;
    @JsonProperty("modificationDate")
    private String modificationDate;
    @JsonProperty("stockKeepingUnitId")
    private String stockKeepingUnitId;
    @JsonProperty("countryInfo")
    private List<CountryInfo> countryInfo;
    @JsonProperty("priceInUSD")
    private String priceInUSD;
    @JsonProperty("merchGroupInfo")
    private List<MerchGroupInfo> merchGroupInfo;
    @JsonProperty("localeInfo")
    private List<LocaleInfo> localeInfo;
    @JsonProperty("links")
    private Links links;
    @JsonProperty("errorMessage")
    private String errorMessage;
    private Throwable error;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CountryInfo {
        @JsonProperty("country")
        private String country;
        @JsonProperty("localizedSize")
        private String localizedSize;
        @JsonProperty("localizedSizePrefix")
        private String localizedSizePrefix;
        @JsonProperty("vat")
        private String vat;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocaleInfo {
        @JsonProperty("locale")
        private String locale;
        @JsonProperty("colorDescription")
        private String colorDescription;
        @JsonProperty("fullTitle")
        private String fullTitle;
        @JsonProperty("title")
        private String title;
        @JsonProperty("itemLongDescription")
        private String itemLongDescription;
        @JsonProperty("itemShortDescription")
        private String itemShortDescription;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MerchGroupInfo {
        @JsonProperty("skuUuid")
        private String skuUuid;
        @JsonProperty("merchGroup")
        private String merchGroup;
        @JsonProperty("channels")
        private List<String> channels = null;
        @JsonProperty("productUuid")
        private String productUuid;
        @JsonProperty("publishType")
        private String publishType;
        @JsonProperty("preorder")
        private Boolean preorder;
        @JsonProperty("preorderAvailabilityDate")
        private String preorderAvailabilityDate;
        @JsonProperty("status")
        private String status;
        @JsonProperty("valueAddedServices")
        private List<ValueAddedService> valueAddedServices = null;
        @JsonProperty("offerStartDate")
        private String offerStartDate;
        @JsonProperty("offerEndDate")
        private String offerEndDate;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValueAddedService {
        @JsonProperty("id")
        private String id;
        @JsonProperty("type")
        private String type;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Links {
        @JsonProperty("self")
        private String self;
    }
}

