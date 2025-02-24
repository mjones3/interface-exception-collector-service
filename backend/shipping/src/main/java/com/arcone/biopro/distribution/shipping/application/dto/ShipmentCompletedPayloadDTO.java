package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record ShipmentCompletedPayloadDTO(
    Long shipmentId,
    Long orderNumber,
    String externalOrderId,
    String performedBy,
    String locationCode,
    String locationName,
    String customerCode,
    String customerName,
    String customerType,
    ZonedDateTime createDate,
    List<ShipmentCompletedItemPayloadDTO> lineItems,
    List<ShipmentCompletedServicePayloadDTO> services

) implements Serializable {
}
