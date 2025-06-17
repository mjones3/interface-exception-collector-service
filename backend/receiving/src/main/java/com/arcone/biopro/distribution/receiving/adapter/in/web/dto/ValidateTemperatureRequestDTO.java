package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
public record ValidateTemperatureRequestDTO(
    BigDecimal temperature,
    String temperatureCategory
) implements Serializable {
}
