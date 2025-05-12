package com.arcone.biopro.distribution.recoveredplasmashipping.domain.event;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@ToString
public class RecoveredPlasmaShipmentProcessingEvent implements DomainEvent<RecoveredPlasmaShipment> {

    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "RecoveredPlasmaShipmentProcessing";
    private final RecoveredPlasmaShipment payload;

    public RecoveredPlasmaShipmentProcessingEvent(RecoveredPlasmaShipment recoveredPlasmaShipment){
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.payload = recoveredPlasmaShipment;
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
    public RecoveredPlasmaShipment getPayload() {
        return payload;
    }
}
