package com.arcone.biopro.distribution.orderservice.domain.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class OrderCreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderCreated";
    private OrderCreatedPayload payload;

    public OrderCreatedEvent (){
        // TODO add order details once the domain object is defined.
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.payload = new OrderCreatedPayload(10,"");
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
    public OrderCreatedPayload getPayload() {
        return payload;
    }

    public record OrderCreatedPayload(
        Integer errorCode,
        String errorMessage
    ) implements Serializable {

    }
}
