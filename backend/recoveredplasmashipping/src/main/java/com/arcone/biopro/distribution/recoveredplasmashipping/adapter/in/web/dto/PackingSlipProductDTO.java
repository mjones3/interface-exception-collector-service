package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record PackingSlipProductDTO(
    String unitNumber,
    Integer volume,
    String collectionDate
) implements Serializable {
}
