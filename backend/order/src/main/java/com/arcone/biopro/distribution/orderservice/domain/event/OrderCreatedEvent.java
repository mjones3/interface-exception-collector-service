package com.arcone.biopro.distribution.orderservice.domain.event;

import com.arcone.biopro.distribution.orderservice.domain.model.Order;

import java.time.Instant;
import java.util.UUID;

public class OrderCreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderCreated";
    private Order payload;

    public OrderCreatedEvent (Order order){
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
