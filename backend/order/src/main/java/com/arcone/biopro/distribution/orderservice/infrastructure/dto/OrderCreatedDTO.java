package com.arcone.biopro.distribution.orderservice.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;

@Builder
public record OrderCreatedDTO(
    String evenId,
    String externalId,
    Long orderNumber,
    Instant occurredOn
) implements Serializable {
}
