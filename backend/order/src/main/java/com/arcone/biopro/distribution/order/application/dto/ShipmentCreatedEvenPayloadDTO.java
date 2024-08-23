package com.arcone.biopro.distribution.order.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShipmentCreatedEvenPayloadDTO(
    Long shipmentId,
    Long orderNumber,
    String shipmentStatus
) implements Serializable {
}
