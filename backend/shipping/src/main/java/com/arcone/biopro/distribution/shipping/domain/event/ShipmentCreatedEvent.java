package com.arcone.biopro.distribution.shipping.domain.event;

import com.arcone.biopro.distribution.shipping.domain.model.Shipment;

import java.time.Instant;
import java.util.UUID;

public class ShipmentCreatedEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "ShipmentCreated";
    private Shipment payload;

    public ShipmentCreatedEvent (Shipment shipment){
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.payload = shipment;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredOn() {
        return occurredOn;
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
    public Shipment getPayload() {
        return payload;
    }
}
