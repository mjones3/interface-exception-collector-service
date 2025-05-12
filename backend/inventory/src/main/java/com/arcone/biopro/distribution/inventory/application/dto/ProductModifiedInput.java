package com.arcone.biopro.distribution.inventory.application.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public record ProductModifiedInput(
    String unitNumber,
    String productCode,
    String shortDescription,
    String parentProductCode,
    String productFamily,
    String expirationDate,
    String expirationTime,
    String expirationTimeZone,
    String modificationLocation,
    ZonedDateTime modificationDate,
    Integer volume,
    Integer weight
    ) {
}
