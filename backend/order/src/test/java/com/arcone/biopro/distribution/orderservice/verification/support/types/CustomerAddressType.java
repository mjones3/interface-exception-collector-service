package com.arcone.biopro.distribution.orderservice.verification.support.types;

import lombok.Builder;

@Builder
public record CustomerAddressType(
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
