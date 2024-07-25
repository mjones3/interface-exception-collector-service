package com.arcone.biopro.distribution.orderservice.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderItemCreatedDTO(
    String productFamily,
    String bloodType,
    Integer quantity,
    String comments
) implements Serializable {
}
