package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record LocationFilterDTO(
    String name,
    String code
) {
}
