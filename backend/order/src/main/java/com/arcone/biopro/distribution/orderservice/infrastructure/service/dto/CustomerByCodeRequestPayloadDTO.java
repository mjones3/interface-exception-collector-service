package com.arcone.biopro.distribution.orderservice.infrastructure.service.dto;

import lombok.Builder;

@Builder
public record CustomerByCodeRequestPayloadDTO(
    String code
) {}
