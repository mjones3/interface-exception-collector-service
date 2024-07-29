package com.arcone.biopro.distribution.order.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.controller.CustomerController;
import com.arcone.biopro.distribution.order.infrastructure.service.CustomerServiceMock;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerAddressDTO;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringJUnitConfig(classes = { CustomerController.class })
class CustomerControllerTest {

    private static final CustomerDTO[] CUSTOMERS = new CustomerDTO[] {
        new CustomerDTO("A1", "externalId1", "name1", "departmentCode1", "departmentName1", "111-111-1111", List.of(new CustomerAddressDTO("contactName1", "addressType1", "state1", "postalCode1", "countryCode1", "city1", "district1", "addressLine11", "addressLine21", "Y")), "Y"),
        new CustomerDTO("B2", "externalId2", "name2", "departmentCode2", "departmentName2", "222-222-2222", List.of(new CustomerAddressDTO("contactName2", "addressType2", "state2", "postalCode2", "countryCode2", "city2", "district2", "addressLine12", "addressLine22", "Y")), "Y"),
        new CustomerDTO("C3", "externalId3", "name3", "departmentCode3", "departmentName3", "333-333-3333", List.of(new CustomerAddressDTO("contactName3", "addressType3", "state3", "postalCode3", "countryCode3", "city3", "district3", "addressLine13", "addressLine23", "Y")), "Y"),
    };

    @Autowired
    CustomerController customerController;

    @MockBean
    CustomerServiceMock customerServiceMock;

    @Test
    void testFindAllCustomers() {
        // Arrange
        var customerCodes = Arrays.stream(CUSTOMERS)
            .map(CustomerDTO::externalId)
            .toList();

        given(this.customerServiceMock.getCustomers())
            .willReturn(Flux.just(CUSTOMERS));

        // Act
        var response = this.customerController.findAllCustomers()
            .toStream()
            .toArray(CustomerDTO[]::new);

        // Assert
        assertEquals(response.length, CUSTOMERS.length);
        assertTrue(
            Arrays.stream(CUSTOMERS)
                .map(CustomerDTO::externalId)
                .allMatch(customerCodes::contains)
        );
    }

    @ValueSource(strings = { "A1", "B2", "C3" })
    @ParameterizedTest
    void testFindCustomerByCode(final String code) {
        // Arrange
        given(this.customerServiceMock.getCustomerByCode(code))
            .willReturn(
                Arrays.stream(CUSTOMERS)
                    .filter(customer -> code.equals(customer.code()))
                    .map(Mono::just)
                    .findFirst()
                    .orElseThrow()
            );

        // Act
        var response = this.customerController.findCustomerByCode(code).block();

        // Assert
        assertNotNull(response);
        assertEquals(response.code(), code);
    }

    @Test
    void testFindCustomerByUnknownExternalId() {
        // Arrange
        var unknownExternalId = "unknownExternalId";
        given(this.customerServiceMock.getCustomerByCode(unknownExternalId))
            .willReturn(Mono.error(new NoSuchElementException("customer-not-found.error")));

        // Assert
        assertThrows(RuntimeException.class, () -> this.customerController.findCustomerByCode(unknownExternalId).block());
    }

}
