package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.CancelOrderCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CancelOrderCommandTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CancelOrderCommand(null,null,null), "External ID cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new CancelOrderCommand("123",null,null), "Employee ID cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new CancelOrderCommand("123","employee-id",null), "Reason cannot be null");

        assertDoesNotThrow(() -> new CancelOrderCommand("1","TEST","COMMENTS" ));

    }

}
