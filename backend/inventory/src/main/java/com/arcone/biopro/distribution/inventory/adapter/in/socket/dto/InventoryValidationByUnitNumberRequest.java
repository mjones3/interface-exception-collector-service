package com.arcone.biopro.distribution.inventory.adapter.in.socket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record InventoryValidationByUnitNumberRequest(
    @NotNull String unitNumber,
    @NotNull String locationCode

) implements Serializable {
}
