package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record AvailableInventoryCriteriaDTO(
    String productFamily,
    String bloodType
) implements Serializable {

}
