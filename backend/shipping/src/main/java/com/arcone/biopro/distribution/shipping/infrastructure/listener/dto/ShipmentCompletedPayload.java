package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentCompletedPayload(

    Long shipmentId,
    Long orderNumber,
    String unitNumber,
    String productCode,
    String performedBy,
    ZonedDateTime createDate

) implements Serializable {

}
