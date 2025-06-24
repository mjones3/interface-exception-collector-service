package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "OrderModified",
    title = "OrderModified",
    description = "Order Modified Event"
)
@Builder
public record OrderModifiedEventDTO(
    @Schema(name = "eventId", title = "Event Id", description = "The event id", example = "bf6f0fca-5141-4367-9761-38828843147e", requiredMode = REQUIRED)
    UUID eventId,
    @Schema(name = "occurredOn", title = "Occurred On", description = "The event occurred on", example = "2025-06-23T17:03:01.211311415Z", requiredMode = REQUIRED)
    Instant occurredOn,
    @Schema(name = "eventVersion", title = "Event Version", description = "The event version", example = "1.0", requiredMode = REQUIRED)
    String eventVersion,
    @Schema(name = "eventType", title = "Event Type", description = "The event type", example = "OrderModified", requiredMode = REQUIRED)
    String eventType,
    @Schema(name = "payload", title = "Payload", description = "The event payload", requiredMode = REQUIRED)
    OrderModifiedPayload payload
) implements Serializable {
}
