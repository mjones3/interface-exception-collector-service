package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import java.time.ZonedDateTime;

public record ProductStored(
    String unitNumber,
    String productCode,
    String deviceStored,
    String deviceUse,
    String storageLocation,
    String location,
    String locationType,
    ZonedDateTime storageTime,
    String performedBy
) {}