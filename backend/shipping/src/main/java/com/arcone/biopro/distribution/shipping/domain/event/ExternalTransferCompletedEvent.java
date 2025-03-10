package com.arcone.biopro.distribution.shipping.domain.event;

import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@ToString
public class ExternalTransferCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "ExternalTransferCompleted";
    private final ExternalTransfer payload;

    public ExternalTransferCompletedEvent (ExternalTransfer externalTransfer){
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.payload = externalTransfer;
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
    public ExternalTransfer getPayload() {
        return payload;
    }
}
