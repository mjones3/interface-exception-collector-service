package com.arcone.biopro.distribution.irradiation.application.irradiation.dto;

import lombok.Builder;

@Builder
public record BatchItemCompletionDTO(
    String unitNumber,
    String productCode,
    boolean isIrradiated
) {
}