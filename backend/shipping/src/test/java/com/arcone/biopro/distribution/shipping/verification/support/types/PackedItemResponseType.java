package com.arcone.biopro.distribution.shipping.verification.support.types;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PackedItemResponseType {
    private Integer id;
    private Integer shipmentItemId;
    private String inventoryId;
    private String unitNumber;
    private String productCode;
    private String aboRh;
    private String productDescription;
    private String productFamily;
    private String expirationDate;
    private String collectionDate;
    private String packedByEmployeeId;
    private String visualInspection;
    private String secondVerification;

}
