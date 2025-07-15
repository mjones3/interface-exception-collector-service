package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record ProductResponseDTO(
    String inventoryId,
    String unitNumber,
    String productCode,
    String aboRh,
    String productDescription,
    String productFamily,
    String status,
    Boolean isLabeled,
    Boolean isLicensed
) {
}
