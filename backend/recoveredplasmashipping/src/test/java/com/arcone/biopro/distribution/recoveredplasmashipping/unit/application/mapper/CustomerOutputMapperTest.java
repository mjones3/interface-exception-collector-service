package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CustomerOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;


import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomerOutputMapperTest {

    private CustomerOutputMapper mapper;
    private Customer customer;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CustomerOutputMapper.class);
        // Setup test data
        Long id = 1L;
        String externalId = "EXT001";
        String customerType = "Regular";
        String name = "John Doe";
        String code = "CUST001";
        String departmentCode = "DEP001";
        String departmentName = "Sales";
        String foreignFlag = "N";
        String phoneNumber = "1234567890";
        String contactName = "Jane Doe";
        String state = "California";
        String postalCode = "90210";
        String country = "United States";
        String countryCode = "US";
        String city = "Los Angeles";
        String district = "Hollywood";
        String addressLine1 = "123 Main St";
        String addressLine2 = "Apt 4B";
        Boolean active = true;
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();

        customer = new Customer(id, externalId, customerType, name, code, departmentCode, departmentName,
            foreignFlag, phoneNumber, contactName, state, postalCode, country, countryCode, city, district,
            addressLine1, addressLine2, active, createDate, modificationDate);

    }

    @Test
    void shouldMapAllCustomerFields() {
        // When
        CustomerOutput output = mapper.toCustomerOutput(customer);

        // Then
        assertNotNull(output);

        assertNotNull(customer);
        assertEquals(output.id(), customer.getId());
        assertEquals(output.externalId(), customer.getExternalId());
        assertEquals(output.customerType(), customer.getCustomerType());
        assertEquals(output.name(), customer.getName());
        assertEquals(output.code(), customer.getCode());
        assertEquals(output.departmentCode(), customer.getDepartmentCode());
        assertEquals(output.departmentName(), customer.getDepartmentName());
        assertEquals(output.foreignFlag(), customer.getForeignFlag());
        assertEquals(output.phoneNumber(), customer.getPhoneNumber());
        assertEquals(output.contactName(), customer.getContactName());
        assertEquals(output.state(), customer.getState());
        assertEquals(output.postalCode(), customer.getPostalCode());
        assertEquals(output.country(), customer.getCountry());
        assertEquals(output.countryCode(), customer.getCountryCode());
        assertEquals(output.city(), customer.getCity());
        assertEquals(output.district(), customer.getDistrict());
        assertEquals(output.addressLine1(), customer.getAddressLine1());
        assertEquals(output.addressLine2(), customer.getAddressLine2());
        assertEquals(output.active(), customer.getActive());
        assertEquals(output.createDate(), customer.getCreateDate());
        assertEquals(output.modificationDate(), customer.getModificationDate());
    }

    @Test
    void shouldHandleNullCustomer() {
        // When
        CustomerOutput output = mapper.toCustomerOutput(null);

        // Then
        assertNull(output);
    }

}
