package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;

import java.util.UUID;

public record InventoryOutput(UUID id, String unitNumber, String productCode, InventoryStatus inventoryStatus, String expirationDate, String location) {
}

