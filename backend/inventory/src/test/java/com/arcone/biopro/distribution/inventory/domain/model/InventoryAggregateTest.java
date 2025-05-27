package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ShipmentType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;
import com.arcone.biopro.distribution.inventory.domain.model.vo.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryAggregateTest {

    private Inventory inventoryMock;
    private InventoryAggregate inventoryAggregate;

    @BeforeEach
    void setUp() {
        inventoryMock = mock(Inventory.class);
        when(inventoryMock.getInventoryLocation()).thenReturn("LOCATION_1");
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(1));

        inventoryAggregate = InventoryAggregate.builder()
            .inventory(inventoryMock)
            .notificationMessages(new ArrayList<>())
            .build();
    }

    @Test
    void testIsExpired_ShouldReturnTrue_WhenExpirationDateIsBeforeNow() {
        when(inventoryMock.isExpired()).thenReturn(true);
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
        when(inventoryMock.isExpired()).thenReturn(true);
        when(inventoryMock.getIsLabeled()).thenReturn(Boolean.TRUE);

        inventoryAggregate.checkIfIsValidToShip("LOCATION_1");

        assertFalse(inventoryAggregate.getNotificationMessages().isEmpty(), "Expected notification messages when inventory is expired");
        NotificationMessage message = inventoryAggregate.getNotificationMessages().get(0);
        assertEquals(MessageType.INVENTORY_IS_EXPIRED.name(), message.name());
    }

    @Test
    void testCheckIfIsValidToShip_ShouldAddNotification_WhenLocationDoesNotMatch() {
        when(inventoryMock.getInventoryStatus()).thenReturn(InventoryStatus.AVAILABLE);
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(1));
        when(inventoryMock.getInventoryLocation()).thenReturn("LOCATION_2");

        inventoryAggregate.checkIfIsValidToShip("LOCATION_1");

        assertFalse(inventoryAggregate.getNotificationMessages().isEmpty(), "Expected notification messages when inventoryLocation does not match");
        NotificationMessage message = inventoryAggregate.getNotificationMessages().get(0);
        assertEquals(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION.name(), message.name());
    }

    @Test
    void testCompleteShipment_ShouldTransitionStatusToShipped() {
        inventoryAggregate.completeShipment(ShipmentType.CUSTOMER);
        verify(inventoryMock).transitionStatus(InventoryStatus.SHIPPED, null);
    }

    @Test
    void testCompleteShipment_ShouldTransitionStatusToInTransit() {
        inventoryAggregate.completeShipment(ShipmentType.INTERNAL_TRANSFER);
        verify(inventoryMock).transitionStatus(InventoryStatus.IN_TRANSIT, null);
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
    @DisplayName("Should Add Volumes")
    void shouldAddVolumes() {
        inventoryAggregate.completeProduct(List.of(new Volume("volume", 50, "MILLILITERS")), AboRhType.OP);
        verify(inventoryMock).addVolume("volume", 50, "MILLILITERS");
    }

    @Test
    @DisplayName("Should Fail When Product Is Available And Not Labeled")
    void shouldFailWhenProductIsAvailableAndNotLabeledAndShipped() {
        when(inventoryMock.getInventoryStatus()).thenReturn(InventoryStatus.AVAILABLE);
        when(inventoryMock.getIsLabeled()).thenReturn(Boolean.FALSE);
        when(inventoryMock.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(10));

        inventoryAggregate.checkIfIsValidToShip("LOCATION_1");

        assertFalse(inventoryAggregate.getNotificationMessages().isEmpty());
        NotificationMessage message = inventoryAggregate.getNotificationMessages().get(0);
        assertEquals(MessageType.INVENTORY_IS_UNLABELED.name(), message.name());
    }

    @Test
    @DisplayName("isAvailable should return true when inventory status is AVAILABLE")
    void testIsAvailable_WhenStatusIsAvailable() {
        when(inventoryMock.getInventoryStatus()).thenReturn(InventoryStatus.AVAILABLE);

        assertTrue(inventoryAggregate.isAvailable());
        verify(inventoryMock).getInventoryStatus();
    }

    @Test
    @DisplayName("isAvailable should return false when inventory status is not AVAILABLE")
    void testIsAvailable_WhenStatusIsNotAvailable() {
        when(inventoryMock.getInventoryStatus()).thenReturn(InventoryStatus.SHIPPED);

        assertFalse(inventoryAggregate.isAvailable());
        verify(inventoryMock).getInventoryStatus();
    }

    @Test
    @DisplayName("getIsLabeled should return inventory labeled status")
    void testGetIsLabeled() {
        when(inventoryMock.getIsLabeled()).thenReturn(true);

        assertTrue(inventoryAggregate.getIsLabeled());
        verify(inventoryMock).getIsLabeled();
    }

    @Test
    @DisplayName("convertProduct should transition status to CONVERTED")
    void testConvertProduct() {
        InventoryAggregate result = inventoryAggregate.convertProduct();

        verify(inventoryMock).transitionStatus(InventoryStatus.CONVERTED, "Child manufactured");
        assertSame(inventoryAggregate, result, "Should return the same instance");
    }

    @Test
    @DisplayName("hasParent should return true when input products list is not empty")
    void testHasParent_WithInputProducts() {
        List<InputProduct> inputProducts = List.of(new InputProduct("W12345678909","PRODUCT1"));
        when(inventoryMock.getInputProducts()).thenReturn(inputProducts);

        assertTrue(inventoryAggregate.hasParent());
        verify(inventoryMock).getInputProducts();
    }

    @Test
    @DisplayName("hasParent should return false when input products list is empty")
    void testHasParent_WithoutInputProducts() {
        when(inventoryMock.getInputProducts()).thenReturn(Collections.emptyList());

        assertFalse(inventoryAggregate.hasParent());
        verify(inventoryMock).getInputProducts();
    }

    @Test
    @DisplayName("label should update labeled status, licensed status and product code")
    void testLabel() {
        String finalProductCode = "E1234V12";
        Boolean isLicensed = true;
        LocalDateTime expirationDate = LocalDateTime.now();

        InventoryAggregate result = inventoryAggregate.label(isLicensed, finalProductCode, expirationDate);

        verify(inventoryMock).setIsLabeled(true);
        verify(inventoryMock).setIsLicensed(isLicensed);
        verify(inventoryMock).setProductCode(argThat(productCode ->
            productCode.value().equals(finalProductCode)
        ));
        assertSame(inventoryAggregate, result, "Should return the same instance");
    }

    @Test
    @DisplayName("label should handle unlicensed products")
    void testLabel_Unlicensed() {
        String finalProductCode = "E1234V12";
        Boolean isLicensed = false;
        LocalDateTime expirationDate = LocalDateTime.now();

        InventoryAggregate result = inventoryAggregate.label(isLicensed, finalProductCode,expirationDate);

        verify(inventoryMock).setIsLabeled(true);
        verify(inventoryMock).setIsLicensed(false);
        verify(inventoryMock).setProductCode(argThat(productCode ->
            productCode.value().equals(finalProductCode)
        ));
        assertSame(inventoryAggregate, result, "Should return the same instance");
    }

    @Test
    @DisplayName("unsuit should transition status to UNSUITABLE with the provided reason")
    void testUnsuit() {
        String reason = "Damaged packaging";

        InventoryAggregate result = inventoryAggregate.unsuit(reason);

        verify(inventoryMock).isConverted();
        verify(inventoryMock).setUnsuitableReason(reason);
        assertSame(inventoryAggregate, result, "Should return the same instance");
    }

    @Test
    @DisplayName("Should Update Inventory Status")
    void shouldUpdateInventoryStatus() {
        InventoryAggregate result = inventoryAggregate.cartonShipped();
        verify(inventoryMock).transitionStatus(InventoryStatus.SHIPPED, null);
        assertSame(inventoryAggregate, result, "Should return the same instance");
    }

}
