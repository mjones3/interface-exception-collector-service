package com.arcone.biopro.distribution.orderservice.unit.domain.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.vo.CustomerAddressType;
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
