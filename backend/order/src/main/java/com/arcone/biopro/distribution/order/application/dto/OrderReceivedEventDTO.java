package com.arcone.biopro.distribution.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

@Schema(
    name = "OrderReceived",
    title = "OrderReceived",
    description = "Order Received Event"
)
@Builder
public record OrderReceivedEventDTO(
    String eventType,
    String eventVersion,
    OrderReceivedEventPayloadDTO payload
) implements Serializable {
}
