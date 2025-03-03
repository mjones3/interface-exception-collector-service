package com.arcone.biopro.distribution.inventory.adapter.in.listener;


import java.time.ZonedDateTime;
import java.util.UUID;

public record EventMessage<T>(String eventId, String eventType, String eventVersion, ZonedDateTime occurredOn, T payload) {
    public EventMessage(String eventType, String eventVersion, T payload) {
        this(UUID.randomUUID().toString(), eventType, eventVersion, ZonedDateTime.now(), payload);
    }
}
