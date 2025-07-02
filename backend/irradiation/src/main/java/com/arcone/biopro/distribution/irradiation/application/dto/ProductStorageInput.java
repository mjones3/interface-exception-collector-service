package com.arcone.biopro.distribution.irradiation.application.dto;

import lombok.Builder;

@Builder
public record ProductStorageInput(
    String unitNumber,
    String productCode,
    String location,
    String deviceStored,
    String storageLocation
    ) {
}
