package com.arcone.biopro.distribution.inventory.application.dto;

import lombok.Builder;

@Builder
public record ProductReceivedInput(
    String unitNumber,
    String productCode,
    String inventoryLocation,
    String quarantines
) {
}
