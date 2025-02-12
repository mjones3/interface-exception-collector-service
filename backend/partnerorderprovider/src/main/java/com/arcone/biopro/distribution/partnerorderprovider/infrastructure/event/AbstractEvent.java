package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public abstract class AbstractEvent<T> {

    @Schema(
        title = "Event ID",
        description = "The event ID",
        example = "ae94cf69-6863-4c26-ad34-99347a9a4c8a",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final UUID eventId;

    @Schema(
        title = "Occurred On",
        description = "When the event occurred",
        example = "2024-10-07T20:59:16.925110827Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final Instant occurredOn;

    @Schema(
        title = "Payload",
        description = "The event payload",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final T payload;

    abstract String getEventType();

    abstract String getEventVersion();

}
