package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderShipmentTest {


    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new OrderShipment(null,null,null,null,null), "Order ID cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new OrderShipment(null,1L,null,null,null), "Shipment id cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new OrderShipment(null,1L,2L,null,null), "Shipment Status cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new OrderShipment(null,1L,2L,"STATUS",null), "Create Date cannot be null");

        assertDoesNotThrow(() -> new OrderShipment(null,1L,2L,"STATUS", ZonedDateTime.now()));

    }
}
