package com.arcone.biopro.distribution.receiving.domain.event;

import com.arcone.biopro.distribution.receiving.domain.model.Import;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@ToString
public class ImportCompletedDomainEvent implements DomainEvent<Import> {


    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "ImportCompleted";
    private final Import payload;

    public ImportCompletedDomainEvent(Import payload){
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
    public Import getPayload() {
        return payload;
    }
}
