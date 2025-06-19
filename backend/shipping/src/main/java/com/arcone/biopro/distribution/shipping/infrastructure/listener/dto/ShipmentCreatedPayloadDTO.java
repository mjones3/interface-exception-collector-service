package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShipmentCreatedPayloadDTO(
    Long shipmentId,
    Long orderNumber,
    String shipmentStatus
) implements Serializable {

}
