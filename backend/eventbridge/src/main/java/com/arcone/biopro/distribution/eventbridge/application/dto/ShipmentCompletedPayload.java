package com.arcone.biopro.distribution.eventbridge.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record ShipmentCompletedPayload(

    Long shipmentId,
    Long orderNumber,
    String externalOrderId,
    String performedBy,
    String locationCode,
    String locationName,
    String customerCode,
    String customerType,
    ZonedDateTime createDate,
    List<ShipmentCompletedItemPayload> lineItems,
    List<ShipmentCompletedServicePayload> services
) implements Serializable {

}
