package com.arcone.biopro.distribution.eventbridge.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ShipmentCompletedItemPayload(

    String productFamily,
    Integer quantity,
    String bloodType,
    List<ShipmentCompletedItemProductPayload> products

) implements Serializable {
}
