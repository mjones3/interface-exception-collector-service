package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentCompletedPayloadDTO(
    Long shipmentId,
    Long orderNumber,
    String unitNumber,
    String productCode,
    String bloodType,
    String productFamily,
    String performedBy,
    ZonedDateTime createDate
) implements Serializable {
}
