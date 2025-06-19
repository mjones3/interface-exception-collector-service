package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.CompleteOrderCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompleteOrderCommandTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CompleteOrderCommand(null,null,null , null), "Order ID cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new CompleteOrderCommand(1L,null,null , null), "employeeId cannot be null or empty");
        assertThrows(IllegalArgumentException.class, () -> new CompleteOrderCommand(1L,"",null , null), "employeeId cannot be null or empty");
        assertThrows(IllegalArgumentException.class, () -> new CompleteOrderCommand(1L,"employee-id",null , null), "create back order cannot be null");

        assertDoesNotThrow(() -> new CompleteOrderCommand(1L,"TEST","COMMENTS" , Boolean.TRUE));

    }
}
