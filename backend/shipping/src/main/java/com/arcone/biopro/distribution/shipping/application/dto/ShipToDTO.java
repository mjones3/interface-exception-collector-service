package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShipToDTO(
    String customerCode,
    String customerName,
    String department,
    String addressLine1,
    String addressLine2,
    String addressComplement,
    String phoneNumber


) implements Serializable {
}
