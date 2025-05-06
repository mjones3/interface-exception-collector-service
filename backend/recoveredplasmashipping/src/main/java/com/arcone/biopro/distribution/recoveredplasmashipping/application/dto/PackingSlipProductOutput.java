package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record PackingSlipProductOutput(
    String unitNumber,
    Integer volume,
    String collectionDate

) implements Serializable {
}
