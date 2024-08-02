package com.arcone.biopro.distribution.inventory.adapter.in.listener.label;

public record LabelAppliedMessage(
    String unitNumber,
    String productCode,
    String expirationDate,
    String location,
    String aboRh) {
}
