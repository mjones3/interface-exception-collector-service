package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ValidateBarcodeCommandInput(
    String barcodeValue,
    String barcodePattern,
    String temperatureCategory
) implements Serializable {
}
