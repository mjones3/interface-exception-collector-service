package com.arcone.biopro.distribution.order.unit.domain.model;

import com.arcone.biopro.distribution.order.domain.model.CancelOrderCommand;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CancelOrderCommandTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CancelOrderCommand(null,null,null,null), "External ID cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new CancelOrderCommand("123","employee-id",null,null), "Reason cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new CancelOrderCommand("123","employee-id","COMMENTS",null), "Cancel Date cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new CancelOrderCommand("123","employee-id","COMMENTS","2025-01-01"), "Cancel Date cannot be invalid");

        assertThrows(IllegalArgumentException.class, () -> new CancelOrderCommand("123","employee-id","COMMENTS", LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))), "Cancel Date cannot be in the future");

        assertDoesNotThrow(() -> new CancelOrderCommand("1","TEST","COMMENTS","2025-01-01 11:09:55" ));

    }

}
