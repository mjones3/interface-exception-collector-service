package com.arcone.biopro.distribution.shipping.infrastructure.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record InventoryValidationByUnitNumberRequest(
    @NotNull String unitNumber,
    @NotNull String locationCode

) implements Serializable {
}
