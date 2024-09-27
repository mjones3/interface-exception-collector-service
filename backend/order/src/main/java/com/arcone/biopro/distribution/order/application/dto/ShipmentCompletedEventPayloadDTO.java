package com.arcone.biopro.distribution.order.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentCompletedEventPayloadDTO(
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
