package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShippingSummaryCartonItemOutput(
    String cartonNumber,
    String productCode,
    String productDescription,
    int totalProducts

) implements Serializable {
}
