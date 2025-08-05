package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record PackingListLabelDTO(
    Long shipmentId,
    Long orderNumber,
    String orderIdBase64Barcode,
    String shipmentIdBase64Barcode,
    ZonedDateTime dateTimePacked,
    String packedBy,
    String enteredBy,
    Integer quantity,
    ShipFromDTO shipFrom,
    ShipToDTO shipTo,
    String distributionComments,
    String shipmentType,
    String labelStatus,
    List<ShipmentItemPackedDTO> packedItems


) implements Serializable {

}
