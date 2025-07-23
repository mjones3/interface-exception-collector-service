package com.arcone.biopro.distribution.customer.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class CustomerDto {
    private String externalId;
    private String name;
    private String code;
    private String departmentCode;
    private String departmentName;
    private String phoneNumber;
    private String foreignFlag;
    private Integer type;
    private String status;
    private List<CustomerAddressDto> customerAddresses;
}
