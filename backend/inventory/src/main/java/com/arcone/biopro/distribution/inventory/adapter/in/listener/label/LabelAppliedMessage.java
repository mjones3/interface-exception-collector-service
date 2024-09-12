package com.arcone.biopro.distribution.inventory.adapter.in.listener.label;

public record LabelAppliedMessage(
    String unitNumber,
    String productCode,
    String productDescription,
    String expirationDate,
    String collectionDate,
    String location,
    String productFamily,
    String aboRh) {
}
