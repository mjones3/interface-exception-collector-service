package com.arcone.biopro.distribution.shippingservice.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentItemPackedDTO(
    Long shipmentItemId,
    Integer inventoryId,
    String unitNumber,
    String productCode,
    String aboRh,
    String productDescription,
    String productFamily,
    ZonedDateTime expirationDate,
    ZonedDateTime collectionDate

) implements Serializable {

}
