package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ValidateBarcodeRequestDTO(
    String barcodeValue,
    String barcodePattern,
    String temperatureCategory
) implements Serializable {
}
