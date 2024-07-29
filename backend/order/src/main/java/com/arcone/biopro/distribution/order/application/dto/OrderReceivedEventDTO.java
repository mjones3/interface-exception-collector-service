package com.arcone.biopro.distribution.order.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderReceivedEventDTO(
    String eventType,
    String eventVersion,
    OrderReceivedEventPayloadDTO payload
) implements Serializable {
}
