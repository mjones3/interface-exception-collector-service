package com.arcone.biopro.distribution.orderservice.infrastructure.service.dto;

import lombok.Builder;

@Builder
public record CustomerAddressDTO(
    String contactName,
    String addressType,
    String state,
    String postalCode,
    String countryCode,
    String city,
    String district,
    String addressLine1,
    String addressLine2,
    String active
) {}
