package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CustomerDTO (
    String code,
    String name
) implements Serializable {
}
