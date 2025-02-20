package com.arcone.biopro.distribution.inventory.adapter.in.listener;


import java.time.ZonedDateTime;

public record EventMessage<T>(String eventType, String eventVersion, ZonedDateTime occurredOn, T payload) {
    public EventMessage(String eventType, String eventVersion, T payload) {
        this(eventType, eventVersion, ZonedDateTime.now(), payload);
    }
}
