package com.arcone.biopro.distribution.order.domain.event;

import com.arcone.biopro.distribution.order.domain.model.Order;

import java.time.Instant;
import java.util.UUID;

public class OrderCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderCompleted";
    private Order payload;

    public OrderCompletedEvent(Order order){
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.payload = order;
    }

    @Override
    public UUID getEventId() {
        return this.eventId;
    }

    @Override
    public Instant getOccurredOn() {
        return this.occurredOn;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public String getEventVersion() {
        return eventVersion;
    }

    @Override
    public Order getPayload() {
        return payload;
    }

}
