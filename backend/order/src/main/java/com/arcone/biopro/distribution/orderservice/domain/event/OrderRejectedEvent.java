package com.arcone.biopro.distribution.orderservice.domain.event;

import java.time.Instant;
import java.util.UUID;

public class OrderRejectedEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;

    public OrderRejectedEvent (){
        // TODO add order details once the domain object is defined.
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
    }

    @Override
    public UUID getEventId() {
        return this.eventId;
    }

    @Override
    public Instant getOccurredOn() {
        return this.occurredOn;
    }
}
