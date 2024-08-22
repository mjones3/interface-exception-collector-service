package com.arcone.biopro.distribution.order.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record GetAvailableInventoryCommandDTO(
    String locationCode,
    List<AvailableInventoryCriteriaDTO> availableInventoryCriteriaDTOS
) implements Serializable {


}
