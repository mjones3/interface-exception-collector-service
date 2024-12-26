package com.arcone.biopro.distribution.inventory.infrastructure.event;

import com.arcone.biopro.distribution.inventory.domain.event.InventoryEvent;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventorylEventPublisherImpl implements InventoryEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public <T extends InventoryEvent> void publish(T event) {
        applicationEventPublisher.publishEvent(event);
    }
}
