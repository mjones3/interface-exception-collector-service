package com.arcone.biopro.distribution.shipping.application.dto;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.VisualInspection;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
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
    LocalDateTime expirationDate,
    ZonedDateTime collectionDate,
    String packedByEmployeeId,
    VisualInspection visualInspection

) implements Serializable {

}
