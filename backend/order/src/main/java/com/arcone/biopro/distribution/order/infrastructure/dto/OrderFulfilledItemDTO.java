package com.arcone.biopro.distribution.order.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record OrderFulfilledItemDTO(
    Long id,
    Long orderId,
    String productFamily,
    String bloodType,
    Integer quantity,
    String comments,
    Integer totalAvailable,
    List<OrderFulfilledItemShortDateDTO> shortDateProducts
) implements Serializable {
}
