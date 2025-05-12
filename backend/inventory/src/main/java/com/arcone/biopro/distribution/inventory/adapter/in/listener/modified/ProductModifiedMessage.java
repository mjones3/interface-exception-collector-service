package com.arcone.biopro.distribution.inventory.adapter.in.listener.modified;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.common.Volume;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.created.ValueUnit;

import java.time.ZonedDateTime;

public record ProductModifiedMessage(
    String unitNumber,
    String productCode,
    String shortDescription,
    String parentProductCode,
    String productFamily,
    String expirationDate,
    String expirationTime,
    String modificationLocation,
    ZonedDateTime modificationDate,
    Volume volume,
    ValueUnit weight) {
}
