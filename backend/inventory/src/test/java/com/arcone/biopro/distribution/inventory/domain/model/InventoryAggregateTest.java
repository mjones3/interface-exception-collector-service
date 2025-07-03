package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryAggregateTest {

    private InventoryAggregate inventoryAggregate;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = Inventory.builder()
            .unitNumber(new UnitNumber("W036589878681"))
            .productCode(new ProductCode("E6170V00"))
            .inventoryStatus(InventoryStatus.IN_TRANSIT)
            .inventoryLocation("OLD_LOCATION")
            .build();

        inventoryAggregate = InventoryAggregate.builder()
            .inventory(inventory)
            .build();
    }

    @Test
    void productReceived_ShouldUpdateLocationAndStatus_WhenNoQuarantine() {
        // Arrange
        String newLocation = "NEW_LOCATION";
        Boolean hasQuarantine = false;

        // Act
        inventoryAggregate.productReceived(newLocation, hasQuarantine);

        // Assert
        assertEquals(InventoryStatus.AVAILABLE, inventory.getInventoryStatus());
        assertEquals(newLocation, inventory.getInventoryLocation());
        assertFalse(inventoryAggregate.isQuarantined());
    }

    @Test
    void productReceived_ShouldUpdateLocationStatusAndQuarantine_WhenHasQuarantine() {
        // Arrange
        String newLocation = "NEW_LOCATION";
        Boolean hasQuarantine = true;

        // Act
        inventoryAggregate.productReceived(newLocation, hasQuarantine);

        // Assert
        assertEquals(InventoryStatus.AVAILABLE, inventory.getInventoryStatus());
        assertEquals(newLocation, inventory.getInventoryLocation());
        assertTrue(inventoryAggregate.isQuarantined());
    }
}