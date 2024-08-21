package com.arcone.biopro.distribution.inventory.adapter.in.listener.shipment;

public record ShipmentCompletedMessage(
    String shipmentId,
    String productCode,
    String orderNumber,
    String unitNumber,
    String performedBy) {
}
