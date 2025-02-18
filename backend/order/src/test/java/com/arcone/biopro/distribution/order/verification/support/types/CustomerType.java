package com.arcone.biopro.distribution.order.verification.support.types;

import lombok.Builder;

import java.util.List;

@Builder
public record CustomerType(
    String externalId,
    String name,
    String code,
    String departmentCode,
    String departmentName,
    String phoneNumber,
    String active,
    List<CustomerAddressType> addresses
) {
}
