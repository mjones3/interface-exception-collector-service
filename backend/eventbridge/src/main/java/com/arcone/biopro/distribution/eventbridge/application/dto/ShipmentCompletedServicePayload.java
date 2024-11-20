package com.arcone.biopro.distribution.eventbridge.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShipmentCompletedServicePayload(
    String code,
    Integer quantity
) implements Serializable {
}
