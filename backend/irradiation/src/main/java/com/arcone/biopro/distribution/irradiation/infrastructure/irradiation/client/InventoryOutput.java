package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client;

import com.arcone.biopro.distribution.irradiation.domain.model.Property;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record InventoryOutput(
    String unitNumber,
    String productCode,
    String location,
    String inventoryStatus,
    String productDescription,
    String productFamily,
    LocalDateTime expirationDate,
    String shortDescription,
    boolean isLabeled,
    String statusReason,
    String unsuitableReason,
    Boolean expired,
    List<InventoryQuarantineOutput> quarantines,
    List<Property> properties) {
}

