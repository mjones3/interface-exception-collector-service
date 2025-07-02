package com.arcone.biopro.distribution.irradiation.domain.event;

public interface InventoryEventPublisher {
    <T extends InventoryEvent> void publish(T event);

}
