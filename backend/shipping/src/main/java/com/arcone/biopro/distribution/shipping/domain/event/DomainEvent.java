package com.arcone.biopro.distribution.shipping.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent<T> {
    UUID getEventId();
    Instant getOccurredOn();
    String getEventType();
    String getEventVersion();
    T getPayload();

}
