package com.arcone.biopro.distribution.order.domain.event;

import com.arcone.biopro.distribution.order.domain.model.Order;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@ToString
@EqualsAndHashCode
public class OrderModifiedEvent implements DomainEvent<Order> {

    private final UUID eventId;
    private final Instant occurredOn;
    private final static String eventVersion = "1.0";
    private final static String eventType = "OrderModified";
    private Order payload;

    public OrderModifiedEvent(Order order){
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.payload = order;
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
    public Order getPayload() {
        return payload;
    }
}
