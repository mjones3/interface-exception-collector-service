package com.arcone.biopro.distribution.orderservice.unit.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderCustomer;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.infrastructure.controller.error.DataNotFoundException;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import graphql.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCustomerTest {

    @Test
    public void shouldCreateOrderCustomerCode() {
        CustomerService customerService = Mockito.mock(CustomerService.class);
        CustomerDTO customer = Mockito.mock(CustomerDTO.class);
        Mockito.when(customer.code()).thenReturn("CODE");
        Mockito.when(customer.name()).thenReturn("NAME");
        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(customer));
        var orderCustomer = new OrderCustomer("CODE",customerService);
        Assert.assertNotNull(orderCustomer);
        Assertions.assertEquals("CODE",orderCustomer.getCode());
        Assertions.assertEquals("NAME",orderCustomer.getName());
    }

    @Test
    public void shouldNotCreateOrderCustomerCodeWhenCustomerCodeIsInvalid() {
        CustomerService customerService = Mockito.mock(CustomerService.class);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new OrderCustomer(null, customerService));
        assertEquals("code cannot be null or blank", exception.getMessage());
    }

    @Test
    public void shouldNotCreateOrderCustomerCodeWhenCustomerDoesNotExists() {

        CustomerService customerService = Mockito.mock(CustomerService.class);
        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new OrderCustomer("TEST",customerService));
        assertEquals("Customer not found for code: TEST", exception.getMessage());

    }

    @Test
    public void shouldNotCreateOrderCustomerCodeWhenDatabaseThrowsException() {

        CustomerService customerService = Mockito.mock(CustomerService.class);
        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.error(new DataNotFoundException("Test")));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new OrderCustomer("TEST", customerService));

        assertEquals("Customer not found for code: TEST", exception.getMessage());

    }
}
