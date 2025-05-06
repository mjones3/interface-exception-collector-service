package com.arcone.biopro.distribution.inventory.adapter.in.listener.storage;

public record ProductStoredMessage(
    String unitNumber,
    String productCode,
    String deviceStored,
    String deviceUse,
    String storageLocation,
    String location,
    String locationType,
    String storageTime,
    String performedBy
    ) {
}
