package com.arcone.biopro.partner.order.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Base class for all Kafka events published by the Partner Order Service.
 * Provides common event metadata fields required by the BioPro event schema.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {

    /**
     * Unique identifier for this event instance.
     */
    @JsonProperty("eventId")
    private UUID eventId;

    /**
     * Type of the event (e.g., "OrderReceived", "OrderRejected").
     */
    @JsonProperty("eventType")
    private String eventType;

    /**
     * Version of the event schema.
     */
    @JsonProperty("eventVersion")
    private String eventVersion;

    /**
     * Timestamp when the event occurred.
     */
    @JsonProperty("occurredOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime occurredOn;

    /**
     * Source system that generated the event.
     */
    @JsonProperty("source")
    private String source;

    /**
     * Correlation ID for tracing related events across services.
     */
    @JsonProperty("correlationId")
    private UUID correlationId;

    /**
     * Transaction ID that uniquely identifies the business transaction.
     */
    @JsonProperty("transactionId")
    private UUID transactionId;
}