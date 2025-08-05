package com.arcone.biopro.distribution.irradiation.application.irradiation.dto;

import lombok.Builder;

/**
 * DTO representing a batch item with unit details.
 */
@Builder
public record BatchItemDTO(
    String unitNumber,
    String productCode,
    String lotNumber,
    String bloodCenterName,
    String address,
    String registrationNumber,
    String licenseNumber
) {
}
