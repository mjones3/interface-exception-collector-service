package com.arcone.biopro.distribution.order.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Builder
public record CancelOrderReceivedDTO(
    UUID eventId,
    Instant occurredOn,
    String eventType,
    String eventVersion,
    CancelOrderReceivedPayloadDTO payload
) implements Serializable {
}
