package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InventoryAggregate {

    Inventory inventory;

    public InventoryAggregate createInventory(
        String unitNumber,
        String productCode,
        String shortDescription,
        String expirationDate,
        String collectionDate,
        String location,
        ProductFamily productFamily,
        AboRhType aboRhType) {
        this.inventory = new Inventory(
            new UnitNumber(unitNumber),
            new ProductCode(productCode),
            shortDescription,
            expirationDate,
            collectionDate,
            location,
            productFamily,
            aboRhType);
        return this;
    }
}
