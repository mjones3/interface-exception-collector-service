package com.arcone.biopro.distribution.order.unit.domain;

import com.arcone.biopro.distribution.order.domain.model.CustomerAddressType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerAddressTypeTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddressType(null));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddressType(""));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddressType("NOT_ALLOWED_ADDRESS_TYPE"));
        assertDoesNotThrow(() -> new CustomerAddressType("SHIPPING"));
        assertDoesNotThrow(() -> new CustomerAddressType("BILLING"));
    }

}
