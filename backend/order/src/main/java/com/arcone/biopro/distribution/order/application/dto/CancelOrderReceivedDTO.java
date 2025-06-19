package com.arcone.biopro.distribution.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "CancelOrderReceived",
    title = "CancelOrderReceived",
    description = "Cancel Order Received Event"
)
@Builder
public record CancelOrderReceivedDTO(
    UUID eventId,
    Instant occurredOn,
    String eventType,
    String eventVersion,
    CancelOrderReceivedPayloadDTO payload
) implements Serializable {
}
