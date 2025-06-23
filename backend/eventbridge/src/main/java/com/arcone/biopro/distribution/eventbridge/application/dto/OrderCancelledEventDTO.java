package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "OrderCancelled",
    title = "OrderCancelled",
    description = "Order Cancelled Event"
)
@Builder
public record OrderCancelledEventDTO(
    @Schema(
        name = "eventId",
        title = "Event Id",
        description = "The event id",
        example = "1ddb5b6a-0873-4b91-bcf4-7d3ec83e6de1",
        requiredMode = REQUIRED
    )
    UUID eventId,

    @Schema(
        name = "occurredOn",
        title = "Occurred On",
        description = "The event occurred on",
        example = "2025-06-23T13:03:28.589922092Z",
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
        example = "OrderCancelled",
        requiredMode = REQUIRED
    )
    String eventType,

    @Schema(
        name = "payload",
        title = "Payload",
        description = "The event payload",
        requiredMode = REQUIRED
    )
    OrderPayload payload
) implements Serializable {
}