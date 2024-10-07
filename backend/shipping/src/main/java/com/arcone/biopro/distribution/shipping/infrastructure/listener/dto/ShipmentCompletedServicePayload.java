package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShipmentCompletedServicePayload(
    String code,
    Integer quantity
) implements Serializable {
}
