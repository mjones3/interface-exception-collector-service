package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "OrderRejectedEventDTO",
    title = "OrderRejectedEventDTO",
    description = "Order Rejected Event DTO"
)
@Builder
public record OrderRejectedEventDTO(
    @Schema(name = "eventId", title = "Event ID", description = "The event ID", requiredMode = REQUIRED)
    String eventId,
    @Schema(name = "occurredOn", title = "Occurred on", description = "When the event occurred", requiredMode = REQUIRED)
    Instant occurredOn,
    @Schema(name = "payload", title = "Payload", description = "The event payload", requiredMode = REQUIRED)
    OrderRejectedPayload payload,
    @Schema(name = "eventType", title = "Event type", description = "The event type", requiredMode = REQUIRED)
    String eventType,
    @Schema(name = "eventVersion", title = "Event version", description = "The event version", requiredMode = REQUIRED)
    String eventVersion
) implements Serializable {
}