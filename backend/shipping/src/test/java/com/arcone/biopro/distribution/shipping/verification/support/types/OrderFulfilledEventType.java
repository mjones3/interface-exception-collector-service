package com.arcone.biopro.distribution.shipping.verification.support.types;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderFulfilledEventType(
    UUID eventId,
    Instant occurredOn,
    String eventVersion,
    String eventType,
    ShipmentRequestDetailsResponseType payload
) {
}
