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
public class ShipNode {
    private String id;
    private Address address;
    private Email email;
    private Phone phone;
    private String resourceType;
    private Links links;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class Address {
        private String name;
        private String department;
        private String address1;
        private String address2;
        private String address3;
        private String address4;
        private String address5;
        private String address6;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String shortZipCode;
        private String lattitude;
        private String longitude;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class Email {
        private String primary;
        private String alternate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class Links {
        private Self self;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class Phone {
        private String day;
        private String evening;
        private String mobile;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class Self {
        private String ref;
    }



}


