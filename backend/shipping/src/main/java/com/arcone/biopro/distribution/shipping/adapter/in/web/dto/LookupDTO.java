package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record LookupDTO(
    String type,
    String optionValue,
    String descriptionKey,
    int orderNumber,
    boolean active
) {}
