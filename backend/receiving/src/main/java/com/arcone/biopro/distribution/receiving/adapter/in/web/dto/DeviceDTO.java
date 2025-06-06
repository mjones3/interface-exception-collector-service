package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record DeviceDTO(
    String bloodCenterId,
    String deviceType ,
    String deviceCategory,
    String serialNumber,
    String locationCode,
    String name
) implements Serializable {
}
