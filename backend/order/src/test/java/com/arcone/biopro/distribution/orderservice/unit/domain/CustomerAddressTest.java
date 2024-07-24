package com.arcone.biopro.distribution.orderservice.unit.domain;

import com.arcone.biopro.distribution.orderservice.domain.model.CustomerAddress;
import com.arcone.biopro.distribution.orderservice.domain.model.CustomerAddressType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerAddressTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress(null, null, null, null, null, null, null, null, null, true));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress("", null, "", "", "", "", "", "", "", true));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress("contactName", new CustomerAddressType(null), "", "", "", "", "", "", "", true));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress("contactName", new CustomerAddressType(""), "", "", "", "", "", "", "", true));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress("contactName", new CustomerAddressType("UNKNOWN_ADDRESS_TYPE"), "", "", "", "", "", "", "", true));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress("contactName", new CustomerAddressType("SHIPPING"), "", "", "", "", "", "", "", true));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress("contactName", new CustomerAddressType("SHIPPING"), "state", "", "", "", "", "", "", true));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress("contactName", new CustomerAddressType("SHIPPING"), "state", "postalCode", "", "", "", "", "", true));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress("contactName", new CustomerAddressType("SHIPPING"), "state", "postalCode", "countryCode", "", "", "", "", true));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress("contactName", new CustomerAddressType("SHIPPING"), "state", "postalCode", "countryCode", "city", "", "", "", true));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAddress("contactName", new CustomerAddressType("SHIPPING"), "state", "postalCode", "countryCode", "city", "district", "", "", true));
        assertDoesNotThrow(() -> new CustomerAddress("contactName", new CustomerAddressType("SHIPPING"), "state", "postalCode", "countryCode", "city", "district", "addressLine1", null, true));
        assertDoesNotThrow(() -> new CustomerAddress("contactName", new CustomerAddressType("SHIPPING"), "state", "postalCode", "countryCode", "city", "district", "addressLine1", "", true));
        assertDoesNotThrow(() -> new CustomerAddress("contactName", new CustomerAddressType("BILLING"), "state", "postalCode", "countryCode", "city", "district", "addressLine1", null, true));
        assertDoesNotThrow(() -> new CustomerAddress("contactName", new CustomerAddressType("BILLING"), "state", "postalCode", "countryCode", "city", "district", "addressLine1", "addressLine2", true));
    }

}
