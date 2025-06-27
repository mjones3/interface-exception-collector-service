package com.arcone.biopro.distribution.eventbridge.verification.support;

public interface Topics {
    String SHIPMENT_COMPLETED = "ShipmentCompleted";
    String SHIPMENT_COMPLETED_OUTBOUND = "ShipmentCompletedOutbound";
    String INVENTORY_UPDATED = "InventoryUpdated";
    String INVENTORY_UPDATED_OUTBOUND = "InventoryUpdatedOutbound";
    String RPS_SHIPMENT_CLOSED = "RecoveredPlasmaShipmentClosed";
    String ORDER_CREATED = "OrderCreated";
    String ORDER_CANCELLED = "OrderCancelled";
    String ORDER_MODIFIED = "OrderModified";
}
