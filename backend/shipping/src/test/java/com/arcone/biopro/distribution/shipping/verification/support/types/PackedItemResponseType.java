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
