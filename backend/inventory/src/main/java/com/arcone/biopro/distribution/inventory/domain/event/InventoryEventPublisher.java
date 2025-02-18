package com.arcone.biopro.distribution.inventory.domain.event;

public interface InventoryEventPublisher {
    <T extends InventoryEvent> void publish(T event);

}
