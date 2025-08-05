package com.arcone.biopro.distribution.order.infrastructure.service.dto;

import lombok.Builder;

@Builder
public record CustomerByCodeRequestPayloadDTO(
    String code
) {}
