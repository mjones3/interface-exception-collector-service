package com.arcone.biopro.distribution.eventbridge.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ShipmentCompletedEventDTO(
    UUID eventId,
    Instant occurredOn,
    String eventVersion,
    String eventType,
    ShipmentCompletedPayload payload
) implements Serializable {
}
