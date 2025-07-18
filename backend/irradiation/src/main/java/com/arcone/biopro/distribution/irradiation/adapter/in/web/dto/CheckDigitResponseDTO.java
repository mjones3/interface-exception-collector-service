package com.arcone.biopro.distribution.irradiation.adapter.in.web.dto;

import java.io.Serializable;

public record CheckDigitResponseDTO(
        Boolean isValid) implements Serializable {
}
