package com.arcone.biopro.distribution.shippingservice.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShipFromDTO(
    String bloodCenterCode,
    String bloodCenterName,
    String bloodCenterAddressLine1,
    String bloodCenterAddressLine2,
    String bloodCenterAddressComplement

) implements Serializable {
}
