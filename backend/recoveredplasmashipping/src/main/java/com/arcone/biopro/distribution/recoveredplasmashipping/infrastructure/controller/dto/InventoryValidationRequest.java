package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record InventoryValidationRequest(
    @NotNull String unitNumber,
    @NotNull String productCode,
    @NotNull String locationCode

) implements Serializable {
}
