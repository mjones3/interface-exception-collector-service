package com.arcone.biopro.distribution.inventory.application.dto;

public record InventoryInput(
    String unitNumber,
    String productCode,
    String expirationDate,
    String location) {
}
