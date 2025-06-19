package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record ShipmentCompletedItemPayloadDTO(
    String productFamily,
    Integer quantity,
    String bloodType,
    List<ShipmentCompletedItemProductPayloadDTO> products
) implements Serializable {
}
