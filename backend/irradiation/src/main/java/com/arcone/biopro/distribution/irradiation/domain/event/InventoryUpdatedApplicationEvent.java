package com.arcone.biopro.distribution.irradiation.domain.event;

import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryUpdateType;
import lombok.Builder;

@Builder
public record InventoryUpdatedApplicationEvent(
    Inventory inventory,
    InventoryUpdateType inventoryUpdateType
) implements InventoryEvent {
}
