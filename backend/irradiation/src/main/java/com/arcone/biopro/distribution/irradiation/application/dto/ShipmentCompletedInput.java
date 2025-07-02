package com.arcone.biopro.distribution.irradiation.application.dto;

import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.ShipmentType;
import lombok.Builder;

import java.util.List;

@Builder
public record ShipmentCompletedInput(
    String shipmentId,
    ShipmentType shipmentType,
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

