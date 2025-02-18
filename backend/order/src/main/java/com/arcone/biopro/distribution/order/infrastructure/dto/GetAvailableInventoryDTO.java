package com.arcone.biopro.distribution.order.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record GetAvailableInventoryDTO(
    String locationCode,
    List<AvailableInventoryDTO> inventories

) implements Serializable {

}
