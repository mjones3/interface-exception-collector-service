package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.CloseOrderCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CloseOrderCommandTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CloseOrderCommand(null,null,null,null), "Order ID cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new CloseOrderCommand(1L,null,null,null), "employeeId cannot be null or empty");
        assertThrows(IllegalArgumentException.class, () -> new CloseOrderCommand(1L,"",null,null), "employeeId cannot be null or empty");
        assertThrows(IllegalArgumentException.class, () -> new CloseOrderCommand(1L,"TEST",null,null), "reason cannot be null or empty");
        assertThrows(IllegalArgumentException.class, () -> new CloseOrderCommand(1L,"TEST","",null), "reason cannot be null or empty");


        assertDoesNotThrow(() -> new CloseOrderCommand(1L,"TEST","TEST","COMMENTS"));

    }
}
