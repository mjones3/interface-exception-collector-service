package com.arcone.biopro.distribution.irradiation.application.dto;

import java.util.List;

public record ShipmentCompletedOutput(
    String shipmentId,
    String orderNumber,
    String performedBy,
    List<InventoryOutput> inventoryOutputs) {
}
