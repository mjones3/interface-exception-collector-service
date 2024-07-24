package com.arcone.biopro.distribution.orderservice.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;

@Builder
public record OrderRejectedDTO(
    String eventId,
    String externalId,
    String rejectedReason,
    Instant occurredOn

) implements Serializable {

}
