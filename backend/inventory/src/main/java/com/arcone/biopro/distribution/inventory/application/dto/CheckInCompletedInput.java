package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record CheckInCompletedInput(
    String unitNumber,
    String productCode,
    String productDescription,
    String productFamily,
    String inventoryLocation,
    String collectionLocation,
    String collectionTimeZone,
    ZonedDateTime collectionDate,
    AboRhType aboRh
) {
}
