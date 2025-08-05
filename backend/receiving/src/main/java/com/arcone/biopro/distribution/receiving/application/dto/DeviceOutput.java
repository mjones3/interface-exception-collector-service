package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record DeviceOutput(
   String bloodCenterId,
   String deviceType ,
   String deviceCategory,
   String serialNumber,
   String locationCode,
   String name
) implements Serializable {
}
