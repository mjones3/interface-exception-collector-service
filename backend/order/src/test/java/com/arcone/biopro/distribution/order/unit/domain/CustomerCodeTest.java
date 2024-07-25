package com.arcone.biopro.distribution.order.unit.domain;

import com.arcone.biopro.distribution.order.domain.model.CustomerCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerCodeTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerCode(null));
        assertThrows(IllegalArgumentException.class, () -> new CustomerCode(""));
        assertDoesNotThrow(() -> new CustomerCode("code"));
    }

}
