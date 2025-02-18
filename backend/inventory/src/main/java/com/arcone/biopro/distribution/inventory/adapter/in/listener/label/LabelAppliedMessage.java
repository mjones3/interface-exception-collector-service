package com.arcone.biopro.distribution.inventory.adapter.in.listener.label;

import java.time.ZonedDateTime;

public record LabelAppliedMessage(
    String unitNumber,
    String productCode,
    String productDescription,
    String expirationDate,
    Boolean isLicensed,
    Integer weight,
    ZonedDateTime collectionDate,
    String location,
    String productFamily,
    String aboRh) {
}
