package com.arcone.biopro.distribution.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "ModifyOrderReceived",
    title = "ModifyOrderReceived",
    description = "Modify Order Received Event"
)
@Builder
public record ModifyOrderReceivedDTO(
    UUID eventId,
    Instant occurredOn,
    String eventType,
    String eventVersion,
    ModifyOrderReceivedPayloadDTO payload
) implements Serializable {
}
