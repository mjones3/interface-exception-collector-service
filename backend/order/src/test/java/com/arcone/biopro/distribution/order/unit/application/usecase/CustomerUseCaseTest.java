package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.order.application.mapper.CustomerMapper;
import com.arcone.biopro.distribution.order.application.usecase.CustomerUseCase;
import com.arcone.biopro.distribution.order.domain.model.Customer;
import com.arcone.biopro.distribution.order.domain.repository.CustomerRepository;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerUseCase customerUseCase;

    private Customer mockCustomer;
    private CustomerDTO mockCustomerDTO;

    @BeforeEach
    void setUp() {
        mockCustomer = Mockito.mock(Customer.class);
        mockCustomerDTO = CustomerDTO.builder().build();
    }

    @Test
    void getCustomers_WhenCustomersExist_ReturnsCustomerDTOs() {
        // Given
        when(customerRepository.getCustomers())
            .thenReturn(Flux.just(mockCustomer));
        when(customerMapper.mapToDTO(mockCustomer))
            .thenReturn(mockCustomerDTO);

        // When
        Flux<CustomerDTO> result = customerUseCase.getCustomers();

        // Then
        StepVerifier.create(result)
            .expectNext(mockCustomerDTO)
            .verifyComplete();

        verify(customerRepository).getCustomers();
        verify(customerMapper).mapToDTO(mockCustomer);
    }

    @Test
    void getCustomers_WhenNoCustomersExist_ThrowsNoResultsFoundException() {
        // Given
        when(customerRepository.getCustomers())
            .thenReturn(Flux.empty());

        // When
        Flux<CustomerDTO> result = customerUseCase.getCustomers();

        // Then
        StepVerifier.create(result)
            .expectError(NoResultsFoundException.class)
            .verify();

        verify(customerRepository).getCustomers();
        verify(customerMapper, never()).mapToDTO(Mockito.any(Customer.class));
    }

    @Test
    void getCustomerByCode_WhenCustomerExists_ReturnsCustomerDTO() {
        // Given
        String customerCode = "TEST123";
        when(customerRepository.getCustomerByCode(customerCode))
            .thenReturn(Mono.just(mockCustomer));
        when(customerMapper.mapToDTO(mockCustomer))
            .thenReturn(mockCustomerDTO);

        // When
        Mono<CustomerDTO> result = customerUseCase.getCustomerByCode(customerCode);

        // Then
        StepVerifier.create(result)
            .expectNext(mockCustomerDTO)
            .verifyComplete();

        verify(customerRepository).getCustomerByCode(customerCode);
        verify(customerMapper).mapToDTO(mockCustomer);
    }

    @Test
    void getCustomerByCode_WhenCustomerDoesNotExist_ThrowsNoResultsFoundException() {
        // Given
        String customerCode = "NONEXISTENT";
        when(customerRepository.getCustomerByCode(customerCode))
            .thenReturn(Mono.empty());

        // When
        Mono<CustomerDTO> result = customerUseCase.getCustomerByCode(customerCode);

        // Then
        StepVerifier.create(result)
            .expectError(NoResultsFoundException.class)
            .verify();

        verify(customerRepository).getCustomerByCode(customerCode);
        verify(customerMapper, never()).mapToDTO(Mockito.any(Customer.class));
    }
}

