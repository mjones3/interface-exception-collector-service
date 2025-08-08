package com.arcone.biopro.exception.collector.domain.event.util;

import com.arcone.biopro.exception.collector.domain.event.base.BaseEvent;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Utility class for building consistent event instances with standard metadata.
 * Provides helper methods for setting common event fields.
 */
@Component
public class EventBuilder {

    private static final String SERVICE_NAME = "exception-collector-service";
    private static final String DEFAULT_EVENT_VERSION = "1.0";

    /**
     * Populates base event fields with standard values.
     * 
     * @param event         The event to populate
     * @param eventType     The type of the event
     * @param correlationId The correlation ID for event tracing
     * @param causationId   The causation ID (optional)
     * @param <T>           The event type
     * @return The populated event
     */
    public <T extends BaseEvent> T populateBaseFields(T event, String eventType, String correlationId,
            String causationId) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setEventVersion(DEFAULT_EVENT_VERSION);
        event.setOccurredOn(OffsetDateTime.now());
        event.setSource(SERVICE_NAME);
        event.setCorrelationId(correlationId);
        event.setCausationId(causationId);
        return event;
    }

    /**
     * Populates base event fields with standard values (without causation ID).
     * 
     * @param event         The event to populate
     * @param eventType     The type of the event
     * @param correlationId The correlation ID for event tracing
     * @param <T>           The event type
     * @return The populated event
     */
    public <T extends BaseEvent> T populateBaseFields(T event, String eventType, String correlationId) {
        return populateBaseFields(event, eventType, correlationId, null);
    }

    /**
     * Generates a new correlation ID.
     * 
     * @return A new UUID string for correlation tracking
     */
    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a new event ID.
     * 
     * @return A new UUID string for event identification
     */
    public String generateEventId() {
        return UUID.randomUUID().toString();
    }
}