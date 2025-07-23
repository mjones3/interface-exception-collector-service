package com.arcone.biopro.distribution.customer.application.dto;

import lombok.Data;

@Data
public class CustomerResponseDto {
    private Long id;
    private String externalId;
    private String name;
    private String code;
    private String message;
    private boolean created;
}
