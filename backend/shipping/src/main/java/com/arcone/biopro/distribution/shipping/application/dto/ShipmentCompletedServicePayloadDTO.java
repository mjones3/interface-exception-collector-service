package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ShipmentCompletedServicePayloadDTO(
    String code,
    Integer quantity
) implements Serializable {
}
