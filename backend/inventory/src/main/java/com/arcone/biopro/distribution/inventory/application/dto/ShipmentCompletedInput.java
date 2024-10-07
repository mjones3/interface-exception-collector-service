package com.arcone.biopro.distribution.inventory.application.dto;

import java.util.List;

public record ShipmentCompletedInput(
    String shipmentId,
    String orderNumber,
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

