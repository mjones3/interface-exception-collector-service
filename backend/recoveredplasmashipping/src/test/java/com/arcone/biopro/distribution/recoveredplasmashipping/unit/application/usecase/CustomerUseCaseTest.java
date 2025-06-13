package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CustomerOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.CustomerUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Customer;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CustomerRepository;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerOutputMapper customerOutputMapper;

    @InjectMocks
    private CustomerUseCase customerUseCase;

    private Customer customer;
    private CustomerOutput customerOutput;


    @BeforeEach
    void setUp() {
        customer = Mockito.mock(Customer.class);
        customerOutput = Mockito.mock(CustomerOutput.class);

    }

    @Test
    void findAll_WhenCustomersExist_ShouldReturnCustomerOutputs() {
        // Given
        when(customerRepository.findAll()).thenReturn(Flux.just(customer));
        when(customerOutputMapper.toCustomerOutput(customer)).thenReturn(customerOutput);

        // When/Then
        StepVerifier.create(customerUseCase.findAll())
            .expectNext(customerOutput)
            .verifyComplete();

        verify(customerRepository).findAll();
        verify(customerOutputMapper).toCustomerOutput(customer);
    }

    @Test
    void findAll_WhenNoCustomersExist_ShouldThrowNoResultsFoundException() {
        // Given
        when(customerRepository.findAll()).thenReturn(Flux.empty());

        // When/Then
        StepVerifier.create(customerUseCase.findAll())
            .expectError(NoResultsFoundException.class)
            .verify();

        verify(customerRepository).findAll();
        verifyNoInteractions(customerOutputMapper);
    }

    @Test
    void findById_WhenCustomerExists_ShouldReturnCustomerOutput() {
        // Given
        Long customerId = 1L;
        when(customerRepository.findOneById(customerId)).thenReturn(Mono.just(customer));
        when(customerOutputMapper.toCustomerOutput(customer)).thenReturn(customerOutput);

        // When/Then
        StepVerifier.create(customerUseCase.findById(customerId))
            .expectNext(customerOutput)
            .verifyComplete();

        verify(customerRepository).findOneById(customerId);
        verify(customerOutputMapper).toCustomerOutput(customer);
    }

    @Test
    void findById_WhenCustomerDoesNotExist_ShouldThrowDomainNotFoundForKeyException() {
        // Given
        Long customerId = 999L;
        when(customerRepository.findOneById(customerId)).thenReturn(Mono.empty());

        // When/Then
        StepVerifier.create(customerUseCase.findById(customerId))
            .expectErrorMatches(throwable ->
                throwable instanceof DomainNotFoundForKeyException &&
                    throwable.getMessage().contains(customerId.toString()))
            .verify();

        verify(customerRepository).findOneById(customerId);
        verifyNoInteractions(customerOutputMapper);
    }

    @Test
    void findByCode_WhenCustomerExists_ShouldReturnCustomerOutput() {
        // Given
        String customerCode = "CUST001";
        when(customerRepository.findOneByCode(customerCode)).thenReturn(Mono.just(customer));
        when(customerOutputMapper.toCustomerOutput(customer)).thenReturn(customerOutput);

        // When/Then
        StepVerifier.create(customerUseCase.findByCode(customerCode))
            .expectNext(customerOutput)
            .verifyComplete();

        verify(customerRepository).findOneByCode(customerCode);
        verify(customerOutputMapper).toCustomerOutput(customer);
    }

    @Test
    void findByCode_WhenCustomerDoesNotExist_ShouldThrowDomainNotFoundForKeyException() {
        // Given
        String customerCode = "INVALID_CODE";
        when(customerRepository.findOneByCode(customerCode)).thenReturn(Mono.empty());

        // When/Then
        StepVerifier.create(customerUseCase.findByCode(customerCode))
            .expectErrorMatches(throwable ->
                throwable instanceof DomainNotFoundForKeyException &&
                    throwable.getMessage().contains(customerCode))
            .verify();

        verify(customerRepository).findOneByCode(customerCode);
        verifyNoInteractions(customerOutputMapper);
    }

    @Test
    void findAll_WhenRepositoryThrowsError_ShouldPropagateError() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database error");
        when(customerRepository.findAll()).thenReturn(Flux.error(expectedError));

        // When/Then
        StepVerifier.create(customerUseCase.findAll())
            .expectErrorMatches(throwable ->
                throwable.equals(expectedError))
            .verify();

        verify(customerRepository).findAll();
        verifyNoInteractions(customerOutputMapper);
    }

    @Test
    void findById_WhenRepositoryThrowsError_ShouldPropagateError() {
        // Given
        Long customerId = 1L;
        RuntimeException expectedError = new RuntimeException("Database error");
        when(customerRepository.findOneById(customerId)).thenReturn(Mono.error(expectedError));

        // When/Then
        StepVerifier.create(customerUseCase.findById(customerId))
            .expectErrorMatches(throwable ->
                throwable.equals(expectedError))
            .verify();

        verify(customerRepository).findOneById(customerId);
        verifyNoInteractions(customerOutputMapper);
    }

    @Test
    void findByCode_WhenRepositoryThrowsError_ShouldPropagateError() {
        // Given
        String customerCode = "CUST001";
        RuntimeException expectedError = new RuntimeException("Database error");
        when(customerRepository.findOneByCode(customerCode)).thenReturn(Mono.error(expectedError));

        // When/Then
        StepVerifier.create(customerUseCase.findByCode(customerCode))
            .expectErrorMatches(throwable ->
                throwable.equals(expectedError))
            .verify();

        verify(customerRepository).findOneByCode(customerCode);
        verifyNoInteractions(customerOutputMapper);
    }
}

