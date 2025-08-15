package com.arcone.biopro.exception.collector.domain.event.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

/**
 * Base class for all Kafka events with common fields.
 * Provides standard event metadata including correlation tracking.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseEvent {

    @NotBlank(message = "Event ID is required")
    @JsonProperty("eventId")
    private String eventId;

    @NotBlank(message = "Event type is required")
    @JsonProperty("eventType")
    private String eventType;

    @NotBlank(message = "Event version is required")
    @JsonProperty("eventVersion")
    private String eventVersion;

    @NotNull(message = "Occurred on timestamp is required")
    @JsonProperty("occurredOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime occurredOn;

    @NotBlank(message = "Source is required")
    @JsonProperty("source")
    private String source;

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("causationId")
    private String causationId;
}