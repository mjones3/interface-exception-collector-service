package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
public record ValidateTemperatureCommandInput(
    BigDecimal temperature,
    String temperatureCategory
) implements Serializable {
}
