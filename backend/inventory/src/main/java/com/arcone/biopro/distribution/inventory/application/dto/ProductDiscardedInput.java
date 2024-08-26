package com.arcone.biopro.distribution.inventory.application.dto;

public record ProductDiscardedInput(
    String unitNumber,
    String productCode,
    String reason,
    String comments
    ) {
}
