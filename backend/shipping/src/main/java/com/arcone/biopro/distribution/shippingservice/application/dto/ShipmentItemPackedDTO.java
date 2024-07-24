package com.arcone.biopro.distribution.shippingservice.application.dto;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.VisualInspection;
import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentItemPackedDTO(
    Long id,
    Long shipmentItemId,
    Integer inventoryId,
    String unitNumber,
    String productCode,
    String aboRh,
    String productDescription,
    String productFamily,
    ZonedDateTime expirationDate,
    ZonedDateTime collectionDate,
    String packedByEmployeeId,
    VisualInspection visualInspection

) implements Serializable {

}
