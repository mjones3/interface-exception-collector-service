package com.arcone.biopro.distribution.shipping.verification.support.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackProductResponseType {
    private Integer id;
    private Integer shipmentId;
    private String productFamily;
    private String bloodType;
    private String quantity;
    private String comments;
    private ShipmentItemShortDateResponseType[] shortDateProducts;
    private PackedItemResponseType[] packedItems;
}
