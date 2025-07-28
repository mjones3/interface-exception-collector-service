package com.arcone.biopro.distribution.irradiation.adapter.in.web.dto;

import java.io.Serializable;

public record CheckDigitRequestDTO(
                String unitNumber,
                String checkDigit) implements Serializable {
}
