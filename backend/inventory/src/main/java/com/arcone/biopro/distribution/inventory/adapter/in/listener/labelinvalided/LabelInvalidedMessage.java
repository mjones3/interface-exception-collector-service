package com.arcone.biopro.distribution.inventory.adapter.in.listener.labelinvalided;

public record LabelInvalidedMessage(
    String unitNumber,
    String productCode) {
}
