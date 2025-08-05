package com.arcone.biopro.distribution.inventory.application.dto;

import lombok.Builder;

@Builder
public record ProductDiscardedInput(
    String unitNumber,
    String productCode,
    String reason,
    String comments
    ) {
}
