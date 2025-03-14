package com.arcone.biopro.distribution.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;


@Schema(
    name = "ShipmentCreated",
    title = "ShipmentCreated",
    description = "Shipment Created Event"
)
@Builder
public record ShipmentCreatedEventDTO (

    UUID eventId,
    Instant occurredOn,
    String eventVersion,
    String eventType,
    ShipmentCreatedEvenPayloadDTO payload

) implements Serializable {

}

