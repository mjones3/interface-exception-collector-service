package com.arcone.biopro.distribution.inventory.application.dto;

public record ShipmentCompletedInput(
    String shipmentId,
    String productCode,
    String orderNumber,
    String unitNumber,
    String performedBy) {
}
