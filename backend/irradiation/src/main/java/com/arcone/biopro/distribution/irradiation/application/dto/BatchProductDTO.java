package com.arcone.biopro.distribution.irradiation.application.dto;

import lombok.Builder;

@Builder
public record BatchProductDTO(
    String unitNumber,
    String productCode,
    String productFamily,
    String productDescription,
    String status
) {
}
