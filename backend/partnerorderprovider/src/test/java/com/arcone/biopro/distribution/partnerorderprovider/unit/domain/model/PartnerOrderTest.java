package com.arcone.biopro.distribution.partnerorderprovider.unit.domain.model;

import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrder;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrderItem;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrderPickUpType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartnerOrderTest {

    @Test
    void shouldCreateValidPartnerOrderWithAllRequiredFields() {
        UUID id = UUID.randomUUID();
        PartnerOrderPickUpType pickUpType = new PartnerOrderPickUpType(false, null);

        PartnerOrder order = new PartnerOrder(id, "EXT123", "PENDING", "LOC001", "2024-01-01",
            "EMP001", "STANDARD", "DELIVERY", "GROUND", "MEDICAL", "2024-01-05",
            "CUST001", "BILL001", "Test comments", pickUpType, "PRINTED", false);

        assertEquals(id, order.getId());
        assertEquals("EXT123", order.getExternalId());
        assertEquals("PENDING", order.getOrderStatus());
        assertEquals("LOC001", order.getLocationCode());
    }

    @Test
    void shouldCreateValidInternalTransferOrder() {
        UUID id = UUID.randomUUID();
        PartnerOrderPickUpType pickUpType = new PartnerOrderPickUpType(false, null);

        PartnerOrder order = new PartnerOrder(id, "EXT123", "PENDING", "LOC001", "2024-01-01",
            "EMP001", "INTERNAL_TRANSFER", "DELIVERY", "GROUND", "MEDICAL", "2024-01-05",
            "CUST001", null, "Test comments", pickUpType, "PRINTED", false);

        assertEquals("INTERNAL_TRANSFER", order.getShipmentType());
        assertEquals("PRINTED", order.getLabelStatus());
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        PartnerOrderPickUpType pickUpType = new PartnerOrderPickUpType(false, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            new PartnerOrder(null, "EXT123", "PENDING", "LOC001", "2024-01-01",
                "EMP001", "STANDARD", "DELIVERY", "GROUND", "MEDICAL", "2024-01-05",
                "CUST001", "BILL001", "Test comments", pickUpType, "PRINTED", false)
        );

        assertEquals("ID cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenExternalIdIsNull() {
        UUID id = UUID.randomUUID();
        PartnerOrderPickUpType pickUpType = new PartnerOrderPickUpType(false, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            new PartnerOrder(id, null, "PENDING", "LOC001", "2024-01-01",
                "EMP001", "STANDARD", "DELIVERY", "GROUND", "MEDICAL", "2024-01-05",
                "CUST001", "BILL001", "Test comments", pickUpType, "PRINTED", false)
        );

        assertEquals("External ID cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOrderStatusIsBlank() {
        UUID id = UUID.randomUUID();
        PartnerOrderPickUpType pickUpType = new PartnerOrderPickUpType(false, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            new PartnerOrder(id, "EXT123", "", "LOC001", "2024-01-01",
                "EMP001", "STANDARD", "DELIVERY", "GROUND", "MEDICAL", "2024-01-05",
                "CUST001", "BILL001", "Test comments", pickUpType, "PRINTED", false)
        );

        assertEquals("Status cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInternalTransferMissingLabelStatus() {
        UUID id = UUID.randomUUID();
        PartnerOrderPickUpType pickUpType = new PartnerOrderPickUpType(false, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            new PartnerOrder(id, "EXT123", "PENDING", "LOC001", "2024-01-01",
                "EMP001", "INTERNAL_TRANSFER", "DELIVERY", "GROUND", "MEDICAL", "2024-01-05",
                "CUST001", null, "Test comments", pickUpType, null, false)
        );

        assertEquals("Label Status cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenStandardOrderMissingShippingCustomer() {
        UUID id = UUID.randomUUID();
        PartnerOrderPickUpType pickUpType = new PartnerOrderPickUpType(false, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            new PartnerOrder(id, "EXT123", "PENDING", "LOC001", "2024-01-01",
                "EMP001", "STANDARD", "DELIVERY", "GROUND", "MEDICAL", "2024-01-05",
                null, "BILL001", "Test comments", pickUpType, "PRINTED", false)
        );

        assertEquals("Shipping Customer code cannot be null", exception.getMessage());
    }

    @Test
    void shouldAddItemToOrder() {
        UUID id = UUID.randomUUID();
        PartnerOrderPickUpType pickUpType = new PartnerOrderPickUpType(false, null);
        PartnerOrder order = new PartnerOrder(id, "EXT123", "PENDING", "LOC001", "2024-01-01",
            "EMP001", "STANDARD", "DELIVERY", "GROUND", "MEDICAL", "2024-01-05",
            "CUST001", "BILL001", "Test comments", pickUpType, "PRINTED", false);

        PartnerOrderItem item = new PartnerOrderItem("BLOOD", "O+", 2, "Item comment");
        order.addItem(item);

        assertNotNull(order.getOrderItems());
        assertEquals(1, order.getOrderItems().size());
        assertEquals(item, order.getOrderItems().get(0));
    }

    @Test
    void shouldAddMultipleItemsToOrder() {
        UUID id = UUID.randomUUID();
        PartnerOrderPickUpType pickUpType = new PartnerOrderPickUpType(false, null);
        PartnerOrder order = new PartnerOrder(id, "EXT123", "PENDING", "LOC001", "2024-01-01",
            "EMP001", "STANDARD", "DELIVERY", "GROUND", "MEDICAL", "2024-01-05",
            "CUST001", "BILL001", "Test comments", pickUpType, "PRINTED", false);

        PartnerOrderItem item1 = new PartnerOrderItem("BLOOD", "O+", 2, "Item 1");
        PartnerOrderItem item2 = new PartnerOrderItem("PLASMA", "AB", 1, "Item 2");

        order.addItem(item1);
        order.addItem(item2);

        assertEquals(2, order.getOrderItems().size());
        assertEquals(item1, order.getOrderItems().get(0));
        assertEquals(item2, order.getOrderItems().get(1));
    }
}

