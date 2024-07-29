package com.arcone.biopro.distribution.shipping.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record InventoryMockData(
    List<InventoryResponseDTO> data
) implements Serializable {
}
