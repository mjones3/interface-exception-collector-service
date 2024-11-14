package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ShipmentCompletedOutboundItem(

    String productFamily,
    Integer qtyOrdered,
    Integer qtyFilled,
    List<ShipmentCompletedOutboundItemProduct> products

) implements Serializable {
}
