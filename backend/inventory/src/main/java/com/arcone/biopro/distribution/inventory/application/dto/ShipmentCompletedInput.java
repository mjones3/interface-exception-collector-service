package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ShipmentType;
import lombok.Builder;

import java.util.List;

@Builder
public record ShipmentCompletedInput(
    String shipmentId,
    ShipmentType shipmentType,
    String orderNumber,
    String performedBy,
    String locationCode,
    List<LineItem> lineItems) {

    public record LineItem(
        List<Product> products) {

        public record Product(
            String unitNumber,
            String productCode) {
        }
    }
}