package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShipmentCompletedOutboundService(
    String serviceItemCode,
    Integer quantity
) implements Serializable {
}
