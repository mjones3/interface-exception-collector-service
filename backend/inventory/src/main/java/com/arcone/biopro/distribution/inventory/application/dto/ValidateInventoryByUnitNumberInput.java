package com.arcone.biopro.distribution.inventory.application.dto;

import lombok.Builder;

@Builder
public record ValidateInventoryByUnitNumberInput(
    String unitNumber,
    String inventoryLocation
) {
}
