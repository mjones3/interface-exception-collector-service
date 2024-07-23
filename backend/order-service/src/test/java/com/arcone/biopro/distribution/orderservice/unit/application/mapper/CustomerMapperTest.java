package com.arcone.biopro.distribution.orderservice.unit.application.mapper;

import com.arcone.biopro.distribution.orderservice.application.mapper.CustomerMapper;
import com.arcone.biopro.distribution.orderservice.domain.model.Customer;
import com.arcone.biopro.distribution.orderservice.domain.model.CustomerAddress;
import com.arcone.biopro.distribution.orderservice.domain.model.CustomerAddressType;
import com.arcone.biopro.distribution.orderservice.domain.model.CustomerCode;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerAddressDTO;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = { CustomerMapper.class })
class CustomerMapperTest {

    static final CustomerAddress VALID_SHIPPING_CUSTOMER_ADDRESS = new CustomerAddress("contactName", new CustomerAddressType("SHIPPING"), "state", "postalCode", "countryCode", "city", "district", "addressLine1", "addressLine2", true);
    static final CustomerAddress VALID_BILLING_CUSTOMER_ADDRESS = new CustomerAddress("contactName", new CustomerAddressType("BILLING"), "state", "postalCode", "countryCode", "city", "district", "addressLine1", null, true);
    static final CustomerAddressDTO VALID_SHIPPING_CUSTOMER_ADDRESS_DTO = new CustomerAddressDTO("contactName", "SHIPPING", "state", "postalCode", "countryCode", "city", "district", "addressLine1", "addressLine2", "Y");
    static final CustomerAddressDTO VALID_BILLING_CUSTOMER_ADDRESS_DTO = new CustomerAddressDTO("contactName", "BILLING", "state", "postalCode", "countryCode", "city", "district", "addressLine1", null, "Y");

    @Autowired
    CustomerMapper customerMapper;

    @Test
    void testMapToDTO() {
        // Setup
        var customer = new Customer(new CustomerCode("code"), "externalId", "name", "departmentCode", "departmentName", "phoneNumber", List.of(VALID_SHIPPING_CUSTOMER_ADDRESS, VALID_BILLING_CUSTOMER_ADDRESS), true);

        // Execute
        var result = customerMapper.mapToDTO(customer);

        // Verify
        assertEquals("code", result.code());
        assertEquals("externalId", result.externalId());
        assertEquals("name", result.name());
        assertEquals("departmentCode", result.departmentCode());
        assertEquals("departmentName", result.departmentName());
        assertEquals("phoneNumber", result.phoneNumber());
        result.addresses().forEach(address -> {
            assertEquals("contactName", address.contactName());
            assertTrue("SHIPPING".equals(address.addressType()) || "BILLING".equals(address.addressType()));
            assertEquals("state", address.state());
            assertEquals("postalCode", address.postalCode());
            assertEquals("countryCode", address.countryCode());
            assertEquals("city", address.city());
            assertEquals("district", address.district());
            assertEquals("addressLine1", address.addressLine1());
            assertTrue(address.addressLine2() == null || "addressLine2".equals(address.addressLine2()));
            assertEquals("Y", address.active());
        });
        assertEquals("Y", result.active());
    }

    @Test
    void testMapToDomain() {
        // Setup
        var customerDTO = CustomerDTO.builder()
            .code("code")
            .externalId("externalId")
            .name("name")
            .departmentCode("departmentCode")
            .departmentName("departmentName")
            .phoneNumber("phoneNumber")
            .addresses(List.of(VALID_SHIPPING_CUSTOMER_ADDRESS_DTO, VALID_BILLING_CUSTOMER_ADDRESS_DTO))
            .active("Y")
            .build();

        // Execute
        var result = customerMapper.mapToDomain(customerDTO);

        // Verify
        assertEquals("code", result.getCode().getValue());
        assertEquals("externalId", result.getExternalId());
        assertEquals("name", result.getName());
        assertEquals("departmentCode", result.getDepartmentCode());
        assertEquals("departmentName", result.getDepartmentName());
        assertEquals("phoneNumber", result.getPhoneNumber());
        result.getAddresses().forEach(address -> {
            assertEquals("contactName", address.getContactName());
            assertTrue("SHIPPING".equals(address.getAddressType().getValue()) || "BILLING".equals(address.getAddressType().getValue()));
            assertEquals("state", address.getState());
            assertEquals("postalCode", address.getPostalCode());
            assertEquals("countryCode", address.getCountryCode());
            assertEquals("city", address.getCity());
            assertEquals("district", address.getDistrict());
            assertEquals("addressLine1", address.getAddressLine1());
            assertTrue(address.getAddressLine2() == null || "addressLine2".equals(address.getAddressLine2()));
            assertEquals("Y", address.isActive() ? "Y" : "N");
        });
        assertEquals("Y", result.isActive() ? "Y" : "N");
    }

}
