package com.arcone.biopro.distribution.order.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record AvailableInventoryShortDateDTO(
    String unitNumber,
    String productCode,
    String aboRh,
    String storageLocation

) implements Serializable {
}
