package com.arcone.biopro.distribution.order.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderItemCompletedDTO(
    String productFamily,
    String bloodType,
    Integer quantity,
    Integer quantityShipped,
    Integer quantityRemaining,
    String comments
) implements Serializable {
}
