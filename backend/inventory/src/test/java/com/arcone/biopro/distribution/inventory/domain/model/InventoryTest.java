package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = Inventory.builder()
            .id(UUID.randomUUID())
            .unitNumber(null)
            .productCode(null)
            .shortDescription("Short Description")
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.now().plusDays(5))
            .collectionDate(ZonedDateTime.now())
            .location("Storage A")
            .productFamily("PLASMA_TRANSFUSABLE")
            .statusReason("In Use")
            .aboRh(AboRhType.OP)
            .weight(300)
            .isLicensed(true)
            .createDate(ZonedDateTime.now())
            .modificationDate(ZonedDateTime.now())
            .quarantines(new ArrayList<>())
            .histories(new ArrayList<>())
            .comments("Initial comment")
            .deviceStored("Device A")
            .storageLocation("Location 1")
            .build();
    }

    @Test
    void testCreateHistory() {
        // Act
        inventory.createHistory();

        // Assert
        assertEquals(1, inventory.getHistories().size(), "History should be added");
        History history = inventory.getHistories().get(0);
        assertEquals(InventoryStatus.AVAILABLE, history.inventoryStatus());
        assertEquals("In Use", history.reason());
        assertEquals("Initial comment", history.comments());
    }

    @Test
    @DisplayName("Should Add Quarantine Into The Inventory")
    void testAddQuarantine_ShouldAddQuarantine() {
        // Act
        inventory.addQuarantine(1L, "Contamination", "Detected contamination");

        // Assert
        assertNotNull(inventory.getQuarantines());
        assertEquals(1, inventory.getQuarantines().size(), "Quarantine should be added");

    }

    @Test
    @DisplayName("Should Add Two Quarantines And Update One")
    void testUpdateQuarantine_ShouldUpdateFirstQuarantine() {
        // Act
        inventory.addQuarantine(1L, "Contamination", "Detected contamination");
        inventory.addQuarantine(2L, "Under Investigation", "Product is in investigation");


        inventory.updateQuarantine(1L, "OTHER", "Other Comment");

        // Assert
        assertNotNull(inventory.getQuarantines());
        assertEquals(2, inventory.getQuarantines().size(), "Quarantine should be added");

        Quarantine quarantine = inventory.getQuarantines().stream().filter(q -> q.externId().equals(1L)).findFirst().get();

        assertEquals("OTHER", quarantine.reason(), "Quarantine reason should change to OTHER");
        assertEquals("Other Comment", quarantine.comments(), "Quarantine comments should change to 'Other Comment'");

    }

    @Test
    void testRemoveQuarantine_ShouldRemoveQuarantineAndRestoreStatus() {
        // Arrange
        inventory.addQuarantine(1L, "Contamination", "Detected contamination");

        // Act
        inventory.removeQuarantine(1L);

        // Assert
        assertTrue(inventory.getQuarantines().isEmpty(), "Quarantine should be removed");
        assertEquals(InventoryStatus.AVAILABLE, inventory.getInventoryStatus(), "Status should be restored to the most recent after removing quarantine");
    }

    @Test
    @DisplayName("Should Add Two Quarantines And Remove One")
    void testRemoveQuarantine_ShouldRemoveQuarantineAndKeepSameStatus() {
        // Arrange
        inventory.addQuarantine(1L, "Contamination", "Detected contamination");
        inventory.addQuarantine(2L, "Under Investigation", "Product is in investigation");

        // Act
        inventory.removeQuarantine(1L);

        // Assert
        assertEquals(1, inventory.getQuarantines().size(), "Quarantine should be removed");
    }

    @Test
    void testTransitionStatus_ShouldCreateHistoryAndUpdateStatus() {
        // Act
        inventory.transitionStatus(InventoryStatus.DISCARDED, "Product discarded");

        // Assert
        assertEquals(InventoryStatus.DISCARDED, inventory.getInventoryStatus(), "Expected status to change to DISCARDED");
        assertEquals("Product discarded", inventory.getStatusReason(), "Expected status reason to be updated");
        assertEquals(1, inventory.getHistories().size(), "Expected one history entry after transition");
        History history = inventory.getHistories().get(0);
        assertEquals(InventoryStatus.AVAILABLE, history.inventoryStatus(), "Expected previous status to be saved in history");
        assertEquals("In Use", history.reason(), "Expected previous reason to be saved in history");
    }

}
