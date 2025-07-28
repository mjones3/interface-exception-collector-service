package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record GetUnlabeledProductsRequestDTO(
    @NotNull Long shipmentItemId,
    @NotNull  String unitNumber,
    @NotNull  String locationCode
) implements Serializable {
}
