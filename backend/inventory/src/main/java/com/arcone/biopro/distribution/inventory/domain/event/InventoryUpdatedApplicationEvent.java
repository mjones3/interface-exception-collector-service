package com.arcone.biopro.distribution.inventory.domain.event;

import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import lombok.Builder;

@Builder
public record InventoryUpdatedApplicationEvent(
    Inventory inventory
) implements InventoryEvent {
}
