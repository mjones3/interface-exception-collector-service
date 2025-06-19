package com.arcone.biopro.distribution.order.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record AvailableInventoryDTO(
    String productFamily,
    String aboRh,
    Integer quantityAvailable,
    List<AvailableInventoryShortDateDTO> shortDateProducts
) implements Serializable {

}
