package com.arcone.biopro.distribution.eventbridge.domain.event;

import java.time.Instant;
import java.util.UUID;

public abstract class OrderOutboundEvent<T> implements DomainEvent<T> {

    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final T payload;

    protected OrderOutboundEvent(T payload) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.payload = payload;
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
    public String getEventVersion() {
        return this.eventVersion;
    }

    @Override
    public T getPayload() {
        return this.payload;
    }
}
