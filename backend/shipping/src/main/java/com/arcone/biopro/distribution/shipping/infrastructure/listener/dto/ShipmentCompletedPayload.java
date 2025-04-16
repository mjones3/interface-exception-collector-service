package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

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
    String deliveryType,
    String customerName,
    String customerCode,
    String customerType,
    String departmentCode,
    ZonedDateTime createDate,
    List<ShipmentCompletedItemPayload> lineItems,
    List<ShipmentCompletedServicePayload> services
) implements Serializable {

}
