package com.arcone.biopro.distribution.irradiation.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ConfigurationResponseDTO(
    String key,
    String value
) implements Serializable {
}

