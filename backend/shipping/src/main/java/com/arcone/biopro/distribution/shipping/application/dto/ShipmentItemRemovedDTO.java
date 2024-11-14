package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentItemRemovedDTO(
    Long id,
    Long shipmentId,
    String unitNumber,
    String productCode,
    ZonedDateTime removedDate,
    String removedByEmployeeId,
    String ineligibleStatus



) implements Serializable {

}
