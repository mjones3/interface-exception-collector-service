package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;

import java.time.ZonedDateTime;

public record CheckInCompletedInput(
    String unitNumber,
    String productCode,
    String productDescription,
    String productFamily,
    String location,
    ZonedDateTime collectionDate,
    AboRhType aboRh
) {
}
