package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ProductTypeDTO(
    Integer id,
    String productType,
    String productTypeDescription
) implements Serializable {
}
