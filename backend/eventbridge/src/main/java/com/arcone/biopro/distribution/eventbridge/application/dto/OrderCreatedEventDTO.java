package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "OrderCreated",
    title = "OrderCreated",
    description = "Order Created Event"
)
@Builder
public record OrderCreatedEventDTO(
    @Schema(
        name = "eventId",
        title = "Event Id",
        description = "The event id",
        example = "bb61626e-99df-48af-9ccf-107a5f2f06d0",
        requiredMode = REQUIRED
    )
    UUID eventId,

    @Schema(
        name = "occurredOn",
        title = "Occurred On",
        description = "The event occurred on",
        example = "2025-06-18T18:25:58.951619900Z",
        requiredMode = REQUIRED
    )
    Instant occurredOn,

    @Schema(
        name = "eventVersion",
        title = "Event Version",
        description = "The event version",
        example = "1.0",
        requiredMode = REQUIRED
    )
    String eventVersion,

    @Schema(
        name = "eventType",
        title = "Event Type",
        description = "The event type",
        example = "OrderCreated",
        requiredMode = REQUIRED
    )
    String eventType,

    @Schema(
        name = "payload",
        title = "Payload",
        description = "The event payload",
        requiredMode = REQUIRED
    )
    OrderCreatedPayload payload
) implements Serializable {
}