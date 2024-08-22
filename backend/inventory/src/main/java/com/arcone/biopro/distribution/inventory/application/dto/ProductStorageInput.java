package com.arcone.biopro.distribution.inventory.application.dto;

public record ProductStorageInput(
    String unitNumber,
    String productCode,
    String location,
    String deviceStored,
    String storageLocation
    ) {
}
