package com.arcone.biopro.distribution.inventory.adapter.in.listener;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;

import java.time.ZonedDateTime;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "EventMessage",
    description = "Generic envelope for event messages"
)
public record EventMessage<T>(
    @Schema(
        name = "eventId",
        title = "Event Id",
        description = "The event id",
        example = "70382957-5e49-4fb4-9612-a8adb53cae51",
        requiredMode = REQUIRED
    )
    String eventId,
    @Schema(
        title = "Event Type",
        description = "The event type",
        example = "IventoryUpdated",
        requiredMode = REQUIRED
    )
    String eventType,
    @Schema(
        title = "Event Version",
        description = "The event version",
        example = "1.0",
        requiredMode = REQUIRED
    )
    String eventVersion,
    @Schema(
        name = "occurredOn",
        title = "Occurred On",
        description = "The event occurred on",
        example = "2023-01-01T12:00:00Z",
        requiredMode = REQUIRED
    )
    ZonedDateTime occurredOn,
    @Schema(
        name = "payload",
        description = "The event payload",
        requiredMode = REQUIRED
    )
    T payload) {
    public EventMessage(String eventType, String eventVersion, T payload) {
        this(UUID.randomUUID().toString(), eventType, eventVersion, ZonedDateTime.now(), payload);
    }
}
