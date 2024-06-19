package com.arcone.biopro.distribution.shippingservice.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShippingLabelDTO(

    Long shipmentId,
    Long orderNumber,
    String orderIdBase64Barcode,
    String shipmentIdBase64Barcode,
    ShipToDTO shipTo,
    ShipFromDTO shipFrom,
    ZonedDateTime dateTimePacked



) implements Serializable {

}
