package com.arcone.biopro.distribution.recoveredplasmashipping.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller.CustomerController;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CustomerDtoMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class CustomerControllerTest {

    @Mock
    private CustomerService customerService;


    private CustomerDtoMapper customerDtoMapper;

    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customerDtoMapper = Mappers.getMapper(CustomerDtoMapper.class);
        customerController = new CustomerController(customerService, customerDtoMapper);
    }

    @Test
    void findAll_ShouldReturnAllCustomers() {
        // Arrange
        CustomerOutput customer1 = CustomerOutput.builder().build();
        CustomerOutput customer2 =  CustomerOutput.builder().build();



        Mockito.when(customerService.findAll())
            .thenReturn(Flux.just(customer1, customer2));

        // Act & Assert
        StepVerifier.create(customerController.findAll())
            .expectNextCount(2)
            .verifyComplete();

        Mockito.verify(customerService).findAll();
    }

    @Test
    void findAll_WhenNoCustomers_ShouldReturnEmptyFlux() {
        // Arrange
        Mockito.when(customerService.findAll())
            .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(customerController.findAll())
            .expectNextCount(0)
            .verifyComplete();

        Mockito.verify(customerService).findAll();
    }

    @Test
    void findAll_WhenError_ShouldPropagateError() {
        // Arrange
        RuntimeException exception = new RuntimeException("Database error");
        Mockito.when(customerService.findAll())
            .thenReturn(Flux.error(exception));

        // Act & Assert
        StepVerifier.create(customerController.findAll())
            .expectErrorMatches(throwable ->
                throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Database error"))
            .verify();

        Mockito.verify(customerService).findAll();
    }
}

