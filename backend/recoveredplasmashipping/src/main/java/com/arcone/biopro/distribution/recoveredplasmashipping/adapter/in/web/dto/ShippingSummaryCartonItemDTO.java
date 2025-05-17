package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShippingSummaryCartonItemDTO(
    String cartonNumber,
    String productCode,
    String productDescription,
    int totalProducts
) implements Serializable {
}
