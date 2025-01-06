package com.arcone.biopro.distribution.inventory.adapter.in.listener.shipment;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ShipmentType;

import java.util.List;

public record ShipmentCompletedMessage(
    String shipmentId,
    String orderNumber,
    ShipmentType shipmentType,
    String performedBy,
    List<LineItem> lineItems) {

    public record LineItem(
        List<Product> products) {

        public record Product(
            String unitNumber,
            String productCode) {
        }
    }
}
