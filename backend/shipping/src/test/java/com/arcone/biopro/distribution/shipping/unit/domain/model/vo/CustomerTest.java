package com.arcone.biopro.distribution.shipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.shipping.domain.model.Reason;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Customer;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.error.DataNotFoundException;
import com.arcone.biopro.distribution.shipping.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {


    @Test
    void shouldCreate() {

        var customerService = Mockito.mock(CustomerService.class);
        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(CustomerDTO
            .builder()
            .code("code")
            .name("Name")
            .build()));

        var customer = new Customer("A123", "Test", customerService);
        Assertions.assertEquals("A123", customer.getCode());
    }

    @Test
    void shouldNotCreate() {
        var customerService = Mockito.mock(CustomerService.class);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Customer(null, "Test", customerService), "Code cannot be null");

        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.empty());
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Customer("123", "Test", customerService), "Customer should be valid");

        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(CustomerDTO
            .builder()
            .code("code")
            .build()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Customer("123", null, customerService), "Name cannot be null");

        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(CustomerDTO
            .builder()
            .code("code")
                .name("")
            .build()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Customer("123", "", customerService), "Name cannot be blank");

        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenThrow(new DataNotFoundException("Test"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Customer("123", null, customerService), "Customer should be valid");

    }

}
