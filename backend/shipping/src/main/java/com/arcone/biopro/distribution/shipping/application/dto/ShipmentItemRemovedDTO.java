package com.arcone.biopro.distribution.shipping.application.dto;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.IneligibleStatus;
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
    IneligibleStatus ineligibleStatus



) implements Serializable {

}
