package com.arcone.biopro.distribution.inventory.domain.event;

import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryUpdateType;
import lombok.Builder;
import lombok.ToString;

@Builder
public record InventoryUpdatedApplicationEvent(
    Inventory inventory,
    InventoryUpdateType inventoryUpdateType
) implements InventoryEvent {
}
