package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "ShipmentCompleted",
    title = "ShipmentCompleted",
    description = "Shipment Completed Event"
)
@Builder
public record ShipmentCompletedEventDTO(
    @Schema(
        name = "eventId",
        title = "Event Id",
        description = "The event id",
        example = "74dc42d7-d1d0-4447-a054-ec9948dc016a",
        requiredMode = REQUIRED
    )
    UUID eventId,

    @Schema(
        name = "occurredOn",
        title = "Occurred On",
        description = "The event occurred on",
        example = "2024-10-03T15:44:42.328353258Z",
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
        example = "ShipmentCompleted",
        requiredMode = REQUIRED
    )
    String eventType,

    @Schema(
        name = "payload",
        title = "payload",
        description = "The event payload",
        requiredMode = REQUIRED
    )
    ShipmentCompletedPayload payload
) implements Serializable {
}
