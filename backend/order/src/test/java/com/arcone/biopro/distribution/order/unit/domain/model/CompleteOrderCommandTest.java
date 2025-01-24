package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.CompleteOrderCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompleteOrderCommandTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CompleteOrderCommand(null,null,null), "Order ID cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new CompleteOrderCommand(1L,null,null), "employeeId cannot be null or empty");
        assertThrows(IllegalArgumentException.class, () -> new CompleteOrderCommand(1L,"",null), "employeeId cannot be null or empty");

        assertDoesNotThrow(() -> new CompleteOrderCommand(1L,"TEST","COMMENTS"));

    }
}
