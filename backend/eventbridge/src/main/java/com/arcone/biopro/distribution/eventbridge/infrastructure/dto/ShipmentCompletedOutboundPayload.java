package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

/*
payload
    externalOrderID*
    customerCode*
    customerType*
    customerName --- remove
    shipmentNumber*
    shipmentDate*
    quantityShipped*
    shipmentLocationCode*
    shipmentLocationName*
    lineItem*
        productFamily*
        qtyOrdered*
        qtyFilled*
        products*
            unitNumber*
            productCode*
            bloodType*
            expirationDate*
            attributes [ "key", "value" ]*
            collectionDate*
    services*
        serviceItemCode*
        quantity*
*/

@Builder
public record ShipmentCompletedOutboundPayload(

    Long shipmentNumber,
    String externalOrderId,
    String shipmentLocationCode,
    String shipmentLocationName,
    String customerCode,
    String customerType,
    ZonedDateTime shipmentDate,
    Integer quantityShipped,
    List<ShipmentCompletedOutboundItem> lineItems,
    List<ShipmentCompletedOutboundService> services
) implements Serializable {

}
