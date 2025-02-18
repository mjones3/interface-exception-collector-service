package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record LookupDTO(
    String type,
    String optionValue,
    String descriptionKey,
    int orderNumber,
    boolean active
) {}
