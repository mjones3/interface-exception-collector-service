package com.arcone.biopro.distribution.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "ShipmentCompleted",
    title = "ShipmentCompleted",
    description = "Shipment Completed Event"
)
@Builder
public record ShipmentCompletedEventDTO(
    UUID eventId,
    Instant occurredOn,
    String eventVersion,
    String eventType,
    ShipmentCompletedPayload payload
) implements Serializable {
}
