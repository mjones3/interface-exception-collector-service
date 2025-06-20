package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ValidateDeviceInput(
    String bloodCenterId,
    String locationCode
) implements Serializable {
}
