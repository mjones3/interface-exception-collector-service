package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ErrorMessage;

public record ValidateInventoryOutput(InventoryOutput inventoryOutput, ErrorMessage errorMessage) {
}
