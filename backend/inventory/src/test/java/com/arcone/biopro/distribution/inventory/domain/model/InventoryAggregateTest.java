package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.Location;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InventoryAggregateTest {

    @Test
    @DisplayName("should create an inventory successfully")
    void createInventorySuccess() {
        String unitNumber = "W123456789012";
        String productCode = "E1234V12";
        String expirationDate = "2025-01-08T02:05:45.231Z";
        String location = "MIAMI";

        InventoryAggregate inventoryAggregate = InventoryAggregate.builder().build();
        inventoryAggregate.createInventory(unitNumber, productCode, expirationDate, location);

        Inventory inventory = inventoryAggregate.getInventory();

        assertNotNull(inventory, "Inventory should not be null");
        assertNotNull(inventory.getId(), "Id should not be null");
        assertEquals(new UnitNumber(unitNumber), inventory.getUnitNumber(), "Unit number should match");
        assertEquals(new ProductCode(productCode), inventory.getProductCode(), "Product code should match");
        assertEquals(expirationDate, inventory.getExpirationDate(), "Expiration date should match");
        assertEquals(Location.MIAMI, inventory.getLocation(), "Location should match");
        assertEquals(InventoryStatus.AVAILABLE, inventory.getInventoryStatus(), "Status should match");
    }
}
