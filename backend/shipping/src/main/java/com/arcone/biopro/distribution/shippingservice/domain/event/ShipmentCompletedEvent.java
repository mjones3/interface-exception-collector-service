package com.arcone.biopro.distribution.shippingservice.domain.event;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentCompletedEvent(
    Long shipmentId,
    Long orderNumber,
    String unitNumber,
    String productCode,
    String performedBy,
    ZonedDateTime createDate

) implements Serializable {
}
