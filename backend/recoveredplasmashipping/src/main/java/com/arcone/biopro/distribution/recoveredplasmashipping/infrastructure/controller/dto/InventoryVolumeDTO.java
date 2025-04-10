package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record InventoryVolumeDTO(
    String type,
    Integer value,
    String unit
) implements Serializable {
}
