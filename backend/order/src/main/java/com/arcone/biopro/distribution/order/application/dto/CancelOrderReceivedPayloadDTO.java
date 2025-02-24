package com.arcone.biopro.distribution.order.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.UUID;

@Builder
public record CancelOrderReceivedPayloadDTO(
    UUID id,
    String externalId,
    String cancelDate,
    String cancelEmployeeCode,
    String cancelReason

) implements Serializable {
}
