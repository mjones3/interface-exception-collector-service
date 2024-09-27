package com.arcone.biopro.distribution.order.infrastructure.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class OrderFulfilledEventDTO implements Serializable {

    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderFulfilled";
    private final OrderFulfilledDTO payload;

    public OrderFulfilledEventDTO(OrderFulfilledDTO payload) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.payload = payload;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventVersion() {
        return eventVersion;
    }

    public OrderFulfilledDTO getPayload() {
        return payload;
    }


}


