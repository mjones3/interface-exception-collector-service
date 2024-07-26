package com.arcone.biopro.distribution.orderservice.unit.domain.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.vo.CustomerCode;
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
