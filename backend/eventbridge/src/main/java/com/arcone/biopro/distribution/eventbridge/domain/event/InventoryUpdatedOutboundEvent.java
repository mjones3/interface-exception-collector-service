package com.arcone.biopro.distribution.eventbridge.domain.event;

import com.arcone.biopro.distribution.eventbridge.domain.model.InventoryUpdatedOutbound;

import java.time.Instant;
import java.util.UUID;

public class InventoryUpdatedOutboundEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "InventoryUpdatedOutbound";
    private InventoryUpdatedOutbound payload;

    public InventoryUpdatedOutboundEvent(InventoryUpdatedOutbound payload) {
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
    public String getEventType() {
        return this.eventType;
    }

    @Override
    public String getEventVersion() {
        return this.eventVersion;
    }

    @Override
    public InventoryUpdatedOutbound getPayload() {
        return this.payload;
    }
}
