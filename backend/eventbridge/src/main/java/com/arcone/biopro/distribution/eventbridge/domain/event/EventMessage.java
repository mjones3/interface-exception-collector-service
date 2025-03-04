package com.arcone.biopro.distribution.eventbridge.domain.event;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "EventMessage",
    title = "Event message",
    description = "Produced event message",
    example = "InventoryUpdatedOutbound"
)
public record EventMessage<T>(
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
    ZonedDateTime occurredOn,

    @Schema(
        name = "eventType",
        title = "Event Type",
        description = "The event type",
        example = "InventoryUpdatedOutbound",
        requiredMode = REQUIRED
    )
    String eventType,

    @Schema(
        name = "eventVersion",
        title = "Event Version",
        description = "The event version",
        example = "1.0",
        requiredMode = REQUIRED
    )
    String eventVersion,

    @Schema(
        name = "payload",
        title = "Payload",
        description = "The event payload",
        requiredMode = REQUIRED
    )
    T payload
) {
    public EventMessage(String eventType, String eventVersion, T payload) {
        this(UUID.randomUUID(), ZonedDateTime.now(), eventType, eventVersion, payload);
    }
}
