package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderFulfilledEventDTO (

    UUID eventId,
    Instant occurredOn,
    String eventVersion,
    String eventType,
    OrderFulfilledMessage payload

) implements Serializable {


}
