package com.arcone.biopro.distribution.recoveredplasmashipping.domain.event;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@ToString
public class RecoveredPlasmaCartonCreatedEvent implements DomainEvent<Carton> {

    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "RecoveredPlasmaCartonCreated";
    private final Carton payload;

    public RecoveredPlasmaCartonCreatedEvent(Carton carton){
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.payload = carton;
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
    public Carton getPayload() {
        return payload;
    }
}
