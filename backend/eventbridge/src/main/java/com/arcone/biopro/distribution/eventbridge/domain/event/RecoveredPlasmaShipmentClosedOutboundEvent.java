package com.arcone.biopro.distribution.eventbridge.domain.event;

import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedOutbound;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;


@Getter
@EqualsAndHashCode
@ToString
public class RecoveredPlasmaShipmentClosedOutboundEvent implements DomainEvent<RecoveredPlasmaShipmentClosedOutbound> {

    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "RecoveredPlasmaShipmentClosedOutbound";
    private final RecoveredPlasmaShipmentClosedOutbound payload;


    public RecoveredPlasmaShipmentClosedOutboundEvent(RecoveredPlasmaShipmentClosedOutbound payload) {
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
        return eventType;
    }

    @Override
    public String getEventVersion() {
        return eventVersion;
    }

    @Override
    public RecoveredPlasmaShipmentClosedOutbound getPayload() {
        return payload;
    }
}
