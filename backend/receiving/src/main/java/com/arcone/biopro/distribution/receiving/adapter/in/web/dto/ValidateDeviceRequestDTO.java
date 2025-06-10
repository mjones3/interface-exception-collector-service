package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ValidateDeviceRequestDTO(
    String bloodCenterId,
    String locationCode
) implements Serializable {
}
