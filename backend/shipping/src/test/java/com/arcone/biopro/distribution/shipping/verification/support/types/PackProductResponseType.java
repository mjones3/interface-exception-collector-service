package com.arcone.biopro.distribution.shipping.verification.support.types;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PackProductResponseType {
    private Integer id;
    private Integer shipmentItemId;
    private String productFamily;
    private String bloodType;
    private String quantity;
    private String comments;
    private ShipmentItemShortDateResponseType[] shortDateProducts;
    private PackedItemResponseType[] packedItems;
}
