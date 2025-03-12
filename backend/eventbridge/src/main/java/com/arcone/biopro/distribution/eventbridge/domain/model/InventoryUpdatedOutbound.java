package com.arcone.biopro.distribution.eventbridge.domain.model;


import lombok.Getter;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
public class InventoryUpdatedOutbound {

    private final String updateType;
    private final String unitNumber;
    private final String productCode;
    private final String productDescription;
    private final String productFamily;
    private final String bloodType;
    private final LocalDate expirationDate;
    private final String locationCode;
    private final String storageLocation;
    private final List<String> inventoryStatus;
    private final Map<String, Object> properties;
    private final List<Object> inputProducts;

    public InventoryUpdatedOutbound(String updateType, String unitNumber, String productCode,
                                    String productDescription , String productFamily, String bloodType,
                                    LocalDate expirationDate, String locationCode, String storageLocation,
                                    List<String> inventoryStatus, Map<String, Object> properties, List<Object> inputProducts) {

        Assert.notNull(updateType, "updateType must not be null");
        Assert.notNull(unitNumber, "unitNumber must not be null");
        Assert.notNull(productCode, "productCode must not be null");
        Assert.notNull(productFamily, "productFamily must not be null");
        Assert.notNull(expirationDate, "expirationDate must not be null");
        Assert.notNull(inventoryStatus, "inventoryStatus must not be null");
        Assert.notNull(properties, "properties must not be null");
        Assert.notNull(inputProducts, "inputProducts must not be null");

        this.updateType = updateType;
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.productDescription = productDescription;
        this.productFamily = productFamily;
        this.bloodType = bloodType;
        this.expirationDate = expirationDate;
        this.locationCode = locationCode;
        this.storageLocation = storageLocation;
        this.inventoryStatus = inventoryStatus;
        this.properties = properties;
        this.inputProducts = inputProducts;
    }

}
