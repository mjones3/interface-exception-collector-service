package com.arcone.biopro.distribution.shipping.domain.event;

import com.arcone.biopro.distribution.shipping.application.dto.ShipmentCompletedPayloadDTO;

import java.time.Instant;
import java.util.UUID;


public class ShipmentCompletedEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "ShipmentCompleted";
    private ShipmentCompletedPayloadDTO payload;

    public ShipmentCompletedEvent (ShipmentCompletedPayloadDTO payload){
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.payload = payload;

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
    public ShipmentCompletedPayloadDTO getPayload() {
        return payload;
    }
}
