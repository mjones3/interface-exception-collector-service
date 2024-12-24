package com.arcone.biopro.distribution.inventory.domain.event;

import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;

public record ProductCreatedEvent(InventoryAggregate aggregate) implements InventoryEvent{
}
