package com.arcone.biopro.distribution.shipping.infrastructure.service.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CustomerDTO (
    String code,
    String name
) implements Serializable {
}
