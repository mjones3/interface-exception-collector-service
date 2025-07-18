package com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Value object representing a batch item with unit details.
 */
@Builder
public record BatchItem(
    UnitNumber unitNumber,
    String productCode,
    String lotNumber,
    String newProductCode,
    LocalDateTime expirationDate,
    String productFamily
) {
}