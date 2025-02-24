package com.arcone.biopro.distribution.eventbridge.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Builder
public record InventoryUpdatedEventDTO(
    UUID eventId,
    Instant occurredOn,
    String eventVersion,
    String eventType,
    InventoryUpdatedPayload payload
) implements Serializable {
}
