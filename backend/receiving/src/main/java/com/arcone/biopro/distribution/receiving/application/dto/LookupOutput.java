package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

@Builder
public record LookupOutput(
    Long id,
    String type,
    String optionValue,
    String descriptionKey,
    int orderNumber,
    boolean active
) {}
