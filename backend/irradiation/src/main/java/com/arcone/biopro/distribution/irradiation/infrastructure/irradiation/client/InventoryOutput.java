package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client;

import lombok.Builder;

import java.util.List;

@Builder
public record InventoryOutput(
    String unitNumber,
    String productCode,
    String location,
    String inventoryStatus,
    String productDescription,
    String productFamily,
    String shortDescription,
    boolean isLabeled,
    String statusReason,
    String unsuitableReason,
    Boolean expired,
    List<InventoryQuarantineOutput> quarantines) {
}

