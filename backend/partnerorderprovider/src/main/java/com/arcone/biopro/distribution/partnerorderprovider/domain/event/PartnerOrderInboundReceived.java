package com.arcone.biopro.distribution.partnerorderprovider.domain.event;

import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrder;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@ToString
public class PartnerOrderInboundReceived implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderReceived";
    private PartnerOrder payload;


    public PartnerOrderInboundReceived (PartnerOrder payload){
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
    public PartnerOrder getPayload() {
        return payload;
    }
}
