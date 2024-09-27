package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ShipmentCompletedDTO (
    UUID eventId,
    Instant occurredOn,
    String eventVersion,
    String eventType,
    ShipmentCompletedPayload payload

) implements Serializable {
}

