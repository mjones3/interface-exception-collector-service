package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.NotificationMessage;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate.OTHER_SEE_COMMENTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
            .build();
    }

    @Test
    void testIsExpired_ShouldReturnTrue_WhenExpirationDateIsBeforeNow() {
        // Arrange
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().minusDays(1));

        // Act
        boolean result = inventoryAggregate.isExpired();

        // Assert
        assertTrue(result, "Expected inventory to be expired");
    }

    @Test
    void testIsExpired_ShouldReturnFalse_WhenExpirationDateIsAfterNow() {
        // Arrange
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(1));

        // Act
        boolean result = inventoryAggregate.isExpired();

        // Assert
        assertFalse(result, "Expected inventory to not be expired");
    }

    @Test
    void testCheckIfIsValidToShip_ShouldAddNotification_WhenInventoryIsExpired() {
        // Arrange
        when(inventoryMock.getInventoryStatus()).thenReturn(InventoryStatus.AVAILABLE);
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().minusDays(1));

        // Act
        inventoryAggregate.checkIfIsValidToShip("LOCATION_1");

        // Assert
        assertFalse(inventoryAggregate.getNotificationMessages().isEmpty(), "Expected notification messages when inventory is expired");
    }

    @Test
    void testCheckIfIsValidToShip_ShouldAddNotification_WhenLocationDoesNotMatch() {
        // Arrange
        when(inventoryMock.getInventoryStatus()).thenReturn(InventoryStatus.AVAILABLE);
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(1));
        when(inventoryMock.getLocation()).thenReturn("LOCATION_2");

        // Act
        inventoryAggregate.checkIfIsValidToShip("LOCATION_1");

        // Assert
        assertFalse(inventoryAggregate.getNotificationMessages().isEmpty(), "Expected notification messages when location does not match");
    }



    @Test
    void testCreateQuarantinesNotificationMessage_ShouldReturnCorrectMessages() {
        // Arrange
        Quarantine quarantineMock = mock(Quarantine.class);
        when(quarantineMock.reason()).thenReturn(OTHER_SEE_COMMENTS);
        when(quarantineMock.comment()).thenReturn("Special case");
        when(inventoryMock.getInventoryStatus()).thenReturn(InventoryStatus.QUARANTINED);
        when(inventoryMock.getQuarantines()).thenReturn(List.of(quarantineMock));

        // Act
        inventoryAggregate.checkIfIsValidToShip("LOCATION_1");

        // Assert
        List<NotificationMessage> messages = inventoryAggregate.getNotificationMessages();
        assertEquals(1, messages.size());
        assertEquals(OTHER_SEE_COMMENTS + ": Special case", messages.getFirst().message());
    }
}
