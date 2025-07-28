package com.arcone.biopro.distribution.shipping.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record GetUnlabeledProductsRequest(
    @NotNull Long shipmentItemId,
    @NotNull  String unitNumber,
    @NotNull  String locationCode
) implements Serializable {
}
