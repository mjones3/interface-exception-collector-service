package com.arcone.biopro.distribution.receiving.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class ValidateTemperatureCommand implements Validatable {
    private final BigDecimal temperature;
    private final String temperatureCategory;

    public ValidateTemperatureCommand(BigDecimal temperature, String temperatureCategory) {
        this.temperature = temperature;
        this.temperatureCategory = temperatureCategory;
        checkValid();
    }

    @Override
    public void checkValid() {
        if (temperature == null) {
            throw new IllegalArgumentException("Temperature is required");
        } else if (temperatureCategory == null || temperatureCategory.isBlank()) {
            throw new IllegalArgumentException("Temperature category is required");
        }
    }
}
