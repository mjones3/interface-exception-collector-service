package com.arcone.biopro.distribution.inventory.adapter.in.listener.labelinvalidated;

public record LabelInvalidatedMessage(
    String unitNumber,
    String productCode) {
}
