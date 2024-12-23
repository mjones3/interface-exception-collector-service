package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryAggregateTest {

    private Inventory inventoryMock;
    private InventoryAggregate inventoryAggregate;

    @BeforeEach
    void setUp() {
        inventoryMock = mock(Inventory.class);
        when(inventoryMock.getLocation()).thenReturn("LOCATION_1");
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(1));

        inventoryAggregate = InventoryAggregate.builder()
            .inventory(inventoryMock)
            .notificationMessages(new ArrayList<>())
            .build();
    }

    @Test
    void testIsExpired_ShouldReturnTrue_WhenExpirationDateIsBeforeNow() {
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().minusDays(1));
        assertTrue(inventoryAggregate.isExpired(), "Expected inventory to be expired");
    }

    @Test
    void testIsExpired_ShouldReturnFalse_WhenExpirationDateIsAfterNow() {
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(1));
        assertFalse(inventoryAggregate.isExpired(), "Expected inventory to not be expired");
    }

    @Test
    void testCheckIfIsValidToShip_ShouldAddNotification_WhenInventoryIsExpired() {
        when(inventoryMock.getInventoryStatus()).thenReturn(InventoryStatus.AVAILABLE);
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().minusDays(1));

        inventoryAggregate.checkIfIsValidToShip("LOCATION_1");

        assertFalse(inventoryAggregate.getNotificationMessages().isEmpty(), "Expected notification messages when inventory is expired");
        NotificationMessage message = inventoryAggregate.getNotificationMessages().get(0);
        assertEquals(MessageType.INVENTORY_IS_EXPIRED.name(), message.name());
    }

    @Test
    void testCheckIfIsValidToShip_ShouldAddNotification_WhenLocationDoesNotMatch() {
        when(inventoryMock.getInventoryStatus()).thenReturn(InventoryStatus.AVAILABLE);
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(1));
        when(inventoryMock.getLocation()).thenReturn("LOCATION_2");

        inventoryAggregate.checkIfIsValidToShip("LOCATION_1");

        assertFalse(inventoryAggregate.getNotificationMessages().isEmpty(), "Expected notification messages when location does not match");
        NotificationMessage message = inventoryAggregate.getNotificationMessages().get(0);
        assertEquals(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION.name(), message.name());
    }

    @Test
    void testCompleteShipment_ShouldTransitionStatusToShipped() {
        inventoryAggregate.completeShipment();
        verify(inventoryMock).transitionStatus(InventoryStatus.SHIPPED, null);
    }

    @Test
    void testUpdateStorage_ShouldUpdateDeviceAndLocation() {
        inventoryAggregate.updateStorage("DEVICE_1", "STORAGE_1");
        verify(inventoryMock).setDeviceStored("DEVICE_1");
        verify(inventoryMock).setStorageLocation("STORAGE_1");
    }

    @Test
    void testDiscardProduct_ShouldUpdateStatusAndSetComments() {
        inventoryAggregate.discardProduct("Expired", "Product is expired");
        verify(inventoryMock).transitionStatus(InventoryStatus.DISCARDED, "Expired");
        verify(inventoryMock).setComments("Product is expired");
    }

    @Test
    void testRemoveQuarantine_ShouldRemoveQuarantine() {
        inventoryAggregate.removeQuarantine(1L);
        verify(inventoryMock).removeQuarantine(1L);
    }

    @Test
    void testAddQuarantine_ShouldAddNewQuarantine() {
        inventoryAggregate.addQuarantine(1L, "Contamination", "Detected contamination");
        verify(inventoryMock).addQuarantine(1L, "Contamination", "Detected contamination");
    }

    @Test
    @DisplayName("Should Fail When Product Is Not Labeled And Shipped")
    void shouldFailWhenProductIsNotLabeledAndShipped() {
        when(inventoryMock.getInventoryStatus()).thenReturn(InventoryStatus.SHIPPED);
        when(inventoryMock.getIsLabeled()).thenReturn(Boolean.FALSE);
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(10));

        inventoryAggregate.checkIfIsValidToShip("LOCATION_1");

        assertFalse(inventoryAggregate.getNotificationMessages().isEmpty(), "Expected notification messages when inventory is expired");
        NotificationMessage message = inventoryAggregate.getNotificationMessages().get(0);
        assertEquals(MessageType.INVENTORY_IS_SHIPPED.name(), message.name());
    }

}
