package com.arcone.biopro.distribution.receiving.infrastructure.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractEvent<T> implements Serializable {

    @Schema(
        title = "Event ID",
        description = "The event ID",
        example = "ae94cf69-6863-4c26-ad34-99347a9a4c8a",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String eventId;

    @Schema(
        title = "Occurred On",
        description = "When the event occurred",
        example = "2024-10-07T20:59:16.925110827Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ZonedDateTime occurredOn;

    @Schema(
        title = "Event Type",
        description = "The event type",
        example = "CollectionReceived",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String eventType;

    @Schema(
        title = "Event Version",
        description = "The event version",
        example = "1.0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String eventVersion;

    @Schema(
        title = "Payload",
        description = "The event payload",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private T payload;

}
