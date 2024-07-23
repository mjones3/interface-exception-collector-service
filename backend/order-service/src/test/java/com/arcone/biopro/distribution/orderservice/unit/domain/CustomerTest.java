package com.arcone.biopro.distribution.orderservice.unit.domain;

import com.arcone.biopro.distribution.orderservice.domain.model.Customer;
import com.arcone.biopro.distribution.orderservice.domain.model.CustomerAddress;
import com.arcone.biopro.distribution.orderservice.domain.model.CustomerAddressType;
import com.arcone.biopro.distribution.orderservice.domain.model.CustomerCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerTest {

    static final CustomerAddress VALID_SHIPPING_CUSTOMER_ADDRESS = new CustomerAddress("contactName", new CustomerAddressType("SHIPPING"), "state", "postalCode", "countryCode", "city", "district", "addressLine1", null, true);
    static final CustomerAddress VALID_BILLING_CUSTOMER_ADDRESS = new CustomerAddress("contactName", new CustomerAddressType("BILLING"), "state", "postalCode", "countryCode", "city", "district", "addressLine1", null, true);

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(null, null, null, null, null, null, null, true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode(null), null, null, null, null, null, null, true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode(""), null, null, null, null, null, null, true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode("customerCode"), "", "", "", "", "", emptyList(), true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode("customerCode"), "externalId", "", "", "", "", emptyList(), true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode("customerCode"), "externalId", "name", "", "", "", emptyList(), true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode("customerCode"), "externalId", "name", "departmentCode", "", "", emptyList(), true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode("customerCode"), "externalId", "name", "departmentCode", "departmentName", "", emptyList(), true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode("customerCode"), "externalId", "name", "departmentCode", "departmentName", "99999999", emptyList(), true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode("customerCode"), "externalId", "name", "departmentCode", "departmentName", "99999999", List.of(VALID_SHIPPING_CUSTOMER_ADDRESS), true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode("customerCode"), "externalId", "name", "departmentCode", "departmentName", "99999999", List.of(VALID_BILLING_CUSTOMER_ADDRESS), true));
        assertThrows(IllegalArgumentException.class, () -> new Customer(new CustomerCode("!@#$%^&*()"), "externalId", "name", "departmentCode", "departmentName", "99999999", List.of(VALID_SHIPPING_CUSTOMER_ADDRESS, VALID_BILLING_CUSTOMER_ADDRESS), true));
        assertDoesNotThrow(() -> new Customer(new CustomerCode("customerCode"), "externalId", "name", "departmentCode", "departmentName", "99999999", List.of(VALID_SHIPPING_CUSTOMER_ADDRESS, VALID_BILLING_CUSTOMER_ADDRESS), true));
    }

}
