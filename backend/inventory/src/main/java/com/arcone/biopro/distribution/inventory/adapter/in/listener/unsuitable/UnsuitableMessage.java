package com.arcone.biopro.distribution.inventory.adapter.in.listener.unsuitable;

public record UnsuitableMessage(
    String unitNumber,
    String productCode,
    String reasonKey) {
}
