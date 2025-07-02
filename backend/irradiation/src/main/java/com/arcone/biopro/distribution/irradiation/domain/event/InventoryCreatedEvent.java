package com.arcone.biopro.distribution.irradiation.domain.event;

import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;

public record InventoryCreatedEvent(InventoryAggregate aggregate) implements InventoryEvent{
}
