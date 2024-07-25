package com.arcone.biopro.distribution.orderservice.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderReceivedEventDTO(
    String eventType,
    String eventVersion,
    OrderReceivedEventPayloadDTO payload
) implements Serializable {
}
