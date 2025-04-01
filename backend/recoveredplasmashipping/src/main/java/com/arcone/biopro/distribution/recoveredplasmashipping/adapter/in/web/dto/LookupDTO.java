package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record LookupDTO(
    Long id,
    String type,
    String optionValue,
    String descriptionKey,
    int orderNumber,
    boolean active
) {}
