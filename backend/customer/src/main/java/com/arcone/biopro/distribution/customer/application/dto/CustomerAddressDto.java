package com.arcone.biopro.distribution.customer.application.dto;

import lombok.Data;

@Data
public class CustomerAddressDto {
    private String contactName;
    private String addressType;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String district;
    private String country;
}
