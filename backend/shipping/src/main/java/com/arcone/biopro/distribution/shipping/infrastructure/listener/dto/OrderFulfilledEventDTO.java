package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "OrderFulfilled",
    title = "OrderFulfilled",
    description = "Order Fulfilled Event"
)
@Builder
public record OrderFulfilledEventDTO (

    UUID eventId,
    Instant occurredOn,
    String eventVersion,
    String eventType,
    OrderFulfilledMessage payload

) implements Serializable {


}
