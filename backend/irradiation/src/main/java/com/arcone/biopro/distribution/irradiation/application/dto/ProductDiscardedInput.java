package com.arcone.biopro.distribution.irradiation.application.dto;

import lombok.Builder;

@Builder
public record ProductDiscardedInput(
    String unitNumber,
    String productCode,
    String reason,
    String comments
    ) {
}
