package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ProductTypeOutput(
    Integer id,
    String productType,
    String productTypeDescription
) implements Serializable {
}
