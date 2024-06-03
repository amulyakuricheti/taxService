package com.nike.mptaxmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class PIIResponse {

    private String id;
    private Address address;
    private Recipient recipient;
    private ContactInformation contactInformation;
    private String resourceType;
    private Links links;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String address1;
        private String address2;
        private String address3;
        private String address4;
        private String city;
        private String pickUpLocation;
        private String pickUpLocationType;
        private String country;
        private String state;
        private String zipCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recipient {
        private String firstName;
        private String middleName;
        private String lastName;
        private String alternateFirstName;
        private String alternateLastName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactInformation {
        private String dayPhoneNumber;
        private String email;
        private String eveningPhoneNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Links {
        private Self self;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Self {
        private String ref;
    }
}

