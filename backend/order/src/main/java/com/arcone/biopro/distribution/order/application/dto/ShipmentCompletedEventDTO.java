package com.arcone.biopro.distribution.order.application.dto;

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
    ShipmentCompletedEventPayloadDTO payload
) implements Serializable {
}
