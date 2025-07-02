package com.arcone.biopro.distribution.irradiation.application.dto;

import java.time.ZonedDateTime;
import java.util.Map;

public record ProductModifiedInput(
    String unitNumber,
    String productCode,
    String shortDescription,
    String parentProductCode,
    String productFamily,
    String expirationDate,
    String expirationTime,
    String modificationLocation,
    ZonedDateTime modificationDate,
    Integer volume,
    Integer weight,
    String modificationTimeZone,
    Map<String, String> properties
    ) {
}
