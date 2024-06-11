package com.arcone.biopro.distribution.shippingservice.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShipToDTO(
    Long customerCode,
    String customerName,
    String department,
    String addressLine1,
    String addressLine2,
    String addressComplement

) implements Serializable {
}
