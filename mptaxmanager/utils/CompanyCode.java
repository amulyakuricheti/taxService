package com.nike.mptaxmanager.utils;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class CompanyCode {
    public static final ImmutableMap<String, String> STORE_COUNTRY_COMPANYCODE_MAP = buildStoreCountryCompanyCodeMap();
    public static final Map<String, String> COUNTRY_COMPANYCODE_MAP = buildCountryCompanyCodeMap();

    private CompanyCode() {
    }

    static ImmutableMap<String, String> buildStoreCountryCompanyCodeMap() {
        return ImmutableMap.<String, String>builder()
                .put("AT", "738")
                .put("BE", "747")
                .put("CH", "742")
                .put("CZ", "733")
                .put("DE", "737")
                .put("DK", "731")
                .put("ES", "743")
                .put("FI", "745")
                .put("FR", "741")
                .put("GB", "744")
                .put("GR", "748")
                .put("HR", "728")
                .put("HU", "746")
                .put("IE", "732")
                .put("IL", "713")
                .put("IT", "735")
                .put("LU", "745")
                .put("NL", "745")
                .put("NO", "745")
                .put("PL", "734")
                .put("PT", "736")
                .put("RU", "726")
                .put("SE", "739")
                .put("SI", "729")
                .put("SK", "727")
                .put("TR", "749")
                .build();
    }

    public static String getStoreCompanyCode(String country) {
        return STORE_COUNTRY_COMPANYCODE_MAP.get(country);
    }

    static ImmutableMap<String, String> buildCountryCompanyCodeMap() {
        return ImmutableMap.<String, String>builder()
                .put("AT", "745")
                .put("BE", "745")
                .put("CH", "745")
                .put("CZ", "745")
                .put("DE", "745")
                .put("DK", "745")
                .put("ES", "745")
                .put("FI", "745")
                .put("FR", "745")
                .put("GB", "745")
                .put("GR", "745")
                .put("HU", "745")
                .put("IE", "745")
                .put("IT", "745")
                .put("LU", "745")
                .put("NL", "745")
                .put("NO", "745")
                .put("PL", "745")
                .put("PT", "745")
                .put("SE", "745")
                .put("SI", "745")
                .put("LA", "745")
                .put("RU", "745")
                .put("US", "745")
                .build();
    }

    public static String getCompanyCode(String country) {
        return COUNTRY_COMPANYCODE_MAP.get(country) != null ? COUNTRY_COMPANYCODE_MAP.get(country) : "745";
    }
}

