package com.arcone.biopro.distribution.shippingservice.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShipFromDTO(
    String bloodCenterCode,
    String bloodCenterName,
    String bloodCenterBase64Barcode,
    String bloodCenterAddressLine1,
    String bloodCenterAddressLine2,
    String bloodCenterAddressComplement,
    String phoneNumber

) implements Serializable {
}
