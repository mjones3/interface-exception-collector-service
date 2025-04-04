package com.arcone.biopro.distribution.order.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record AvailableInventoryCriteriaDTO(
    String productFamily,
    String bloodType,
    String temperatureCategory
) implements Serializable {

}
