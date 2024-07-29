package com.arcone.biopro.distribution.order.unit.domain;

import com.arcone.biopro.distribution.order.domain.model.OrderItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, null, null, null, null, null, null, null), "productFamily cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, null, "productFamily", null, null, null, null, null), "bloodType cannot be null or blank");
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, null, "productFamily", "bloodType", null, null, null, null), "quantity cannot be null");
        assertDoesNotThrow(() -> new OrderItem(null, null, "productFamily", "bloodType", 3, null, null, null));
    }

}
