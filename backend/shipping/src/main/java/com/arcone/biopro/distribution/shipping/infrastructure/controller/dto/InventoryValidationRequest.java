package com.arcone.biopro.distribution.shipping.infrastructure.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record InventoryValidationRequest(
    @NotNull String unitNumber,
    @NotNull String productCode,
    @NotNull Integer locationCode

) implements Serializable {
}
