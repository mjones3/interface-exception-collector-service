package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record PickListItemShortDateDTO(
    String unitNumber,
    String productCode,
    String storageLocation
) implements Serializable {
}
