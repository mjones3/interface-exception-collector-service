package com.arcone.biopro.distribution.order.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderFulfilledItemShortDateDTO(
    String unitNumber,
    String productCode,
    String storageLocation
) implements Serializable {
}
