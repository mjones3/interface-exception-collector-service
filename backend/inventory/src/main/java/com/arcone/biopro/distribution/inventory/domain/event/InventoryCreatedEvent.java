package com.arcone.biopro.distribution.inventory.domain.event;

import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;

public record InventoryCreatedEvent(InventoryAggregate aggregate) implements InventoryEvent{
}
