package com.arcone.biopro.distribution.customer.domain.service;

import com.arcone.biopro.distribution.customer.application.dto.CustomerAddressDto;
import com.arcone.biopro.distribution.customer.application.dto.CustomerDto;
import com.arcone.biopro.distribution.customer.application.dto.CustomerResponseDto;
import com.arcone.biopro.distribution.customer.application.mapper.CustomerMapper;
import com.arcone.biopro.distribution.customer.domain.model.Customer;
import com.arcone.biopro.distribution.customer.domain.model.CustomerAddress;
import com.arcone.biopro.distribution.customer.domain.repository.CustomerAddressRepository;
import com.arcone.biopro.distribution.customer.domain.repository.CustomerRepository;
import com.arcone.biopro.distribution.customer.infrastructure.messaging.CustomerEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerAddressRepository customerAddressRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private CustomerEventPublisher eventPublisher;

    @InjectMocks
    private CustomerService customerService;

    private CustomerDto customerDto;
    private Customer customer;
    private CustomerAddressDto addressDto;
    private CustomerAddress address;

    @BeforeEach
    void setUp() {
        // Setup test data
        customerDto = new CustomerDto();
        customerDto.setExternalId("EXT123");
        customerDto.setName("Test Customer");
        customerDto.setCode("CUST001");
        customerDto.setDepartmentCode("DEPT001");
        customerDto.setDepartmentName("Test Department");
        customerDto.setPhoneNumber("1234567890");
        customerDto.setForeignFlag("N");
        customerDto.setType(1);
        customerDto.setStatus("Y");

        addressDto = new CustomerAddressDto();
        addressDto.setContactName("John Doe");
        addressDto.setAddressType("BILLING");
        addressDto.setAddressLine1("123 Main St");
        addressDto.setCity("Test City");
        addressDto.setState("TS");
        addressDto.setPostalCode("12345");
        addressDto.setCountry("USA");

        customerDto.setCustomerAddresses(Collections.singletonList(addressDto));

        customer = new Customer();
        customer.setId(1L);
        customer.setExternalId("EXT123");
        customer.setName("Test Customer");
        customer.setCode("CUST001");
        customer.setDepartmentCode("DEPT001");
        customer.setDepartmentName("Test Department");
        customer.setPhoneNumber("1234567890");
        customer.setForeignFlag("N");
        customer.setCustomerType("1");
        customer.setActive("Y");

        address = new CustomerAddress();
        address.setId(1L);
        address.setCustomerId(1L);
        address.setContactName("John Doe");
        address.setAddressType("BILLING");
        address.setAddressLine1("123 Main St");
        address.setCity("Test City");
        address.setState("TS");
        address.setPostalCode("12345");
        address.setCountry("USA");
        address.setActive("Y");
    }

    @Test
    void upsertCustomer_CustomerExists_UpdatesCustomer() {
        // Given
        when(customerMapper.toEntity(any(CustomerDto.class))).thenReturn(customer);
        when(customerMapper.toEntity(any(CustomerAddressDto.class))).thenReturn(address);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);
        when(customerRepository.findByExternalId("EXT123")).thenReturn(Mono.just(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(customer));
        when(customerAddressRepository.findByCustomerId(1L)).thenReturn(Flux.just(address));
        when(customerAddressRepository.delete(any(CustomerAddress.class))).thenReturn(Mono.empty());
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenReturn(Mono.just(address));

        // When
        Mono<CustomerResponseDto> result = customerService.upsertCustomer(customerDto);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(response ->
                response.getId().equals(1L) &&
                response.getExternalId().equals("EXT123") &&
                response.getName().equals("Test Customer") &&
                response.getCode().equals("CUST001") &&
                !response.isCreated()
            )
            .verifyComplete();

        verify(customerRepository).findByExternalId("EXT123");
        verify(customerRepository, times(2)).save(any(Customer.class));
        verify(customerAddressRepository).findByCustomerId(1L);
        verify(customerAddressRepository).delete(address);
        verify(customerAddressRepository).save(any(CustomerAddress.class));
    }

    @Test
    void upsertCustomer_CustomerDoesNotExist_CreatesCustomer() {
        // Given
        when(customerMapper.toEntity(any(CustomerDto.class))).thenReturn(customer);
        when(customerMapper.toEntity(any(CustomerAddressDto.class))).thenReturn(address);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);
        when(customerRepository.findByExternalId("EXT123"))
            .thenReturn(Mono.empty())
            .thenReturn(Mono.just(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(customer));
        when(customerAddressRepository.save(any(CustomerAddress.class))).thenReturn(Mono.just(address));

        // When
        Mono<CustomerResponseDto> result = customerService.upsertCustomer(customerDto);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(response ->
                response.getId().equals(1L) &&
                response.getExternalId().equals("EXT123") &&
                response.getName().equals("Test Customer") &&
                response.getCode().equals("CUST001") &&
                response.isCreated()
            )
            .verifyComplete();

        verify(customerRepository, times(2)).findByExternalId("EXT123");
        verify(customerRepository).save(customer);
        verify(customerAddressRepository).save(any(CustomerAddress.class));
    }

    @Test
    void upsertCustomer_NoAddresses_DoesNotSaveAddresses() {
        // Given
        customerDto.setCustomerAddresses(null);
        when(customerMapper.toEntity(any(CustomerDto.class))).thenReturn(customer);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);
        when(customerRepository.findByExternalId("EXT123"))
            .thenReturn(Mono.empty())
            .thenReturn(Mono.just(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(customer));

        // When
        Mono<CustomerResponseDto> result = customerService.upsertCustomer(customerDto);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(response ->
                response.getId().equals(1L) &&
                response.getExternalId().equals("EXT123") &&
                response.isCreated()
            )
            .verifyComplete();

        verify(customerAddressRepository, never()).save(any(CustomerAddress.class));
    }

    @Test
    void processBatch_ValidCustomers_ProcessesAllCustomers() {
        // Given
        String batchId = "BATCH001";
        CustomerDto customer1 = new CustomerDto();
        customer1.setExternalId("EXT123");
        CustomerDto customer2 = new CustomerDto();
        customer2.setExternalId("EXT456");
        List<CustomerDto> customers = Arrays.asList(customer1, customer2);

        when(customerMapper.toEntity(customer1)).thenReturn(customer);
        when(customerMapper.toEntity(customer2)).thenReturn(customer);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);
        when(customerRepository.findByExternalId("EXT123")).thenReturn(Mono.just(customer));
        when(customerRepository.findByExternalId("EXT456"))
            .thenReturn(Mono.empty())
            .thenReturn(Mono.just(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(customer));
        when(customerAddressRepository.findByCustomerId(anyLong())).thenReturn(Flux.empty());

        // When
        Mono<Void> result = customerService.processBatch(batchId, customers);

        // Then
        StepVerifier.create(result)
            .verifyComplete();

        verify(eventPublisher, times(2)).publishCustomerCompleted(eq(batchId), anyString());
        verify(eventPublisher, never()).publishCustomerFailed(anyString(), anyString(), anyString());
    }

    @Test
    void processBatch_InvalidCustomer_PublishesFailureEvent() {
        // Given
        String batchId = "BATCH001";
        CustomerDto customer1 = new CustomerDto();
        customer1.setExternalId("EXT123");
        CustomerDto customer2 = new CustomerDto();
        customer2.setExternalId("EXT456");
        List<CustomerDto> customers = Arrays.asList(customer1, customer2);

        when(customerMapper.toEntity(customer1)).thenReturn(customer);
        when(customerMapper.toEntity(customer2)).thenReturn(customer);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);
        when(customerRepository.findByExternalId("EXT123")).thenReturn(Mono.just(customer));
        when(customerRepository.findByExternalId("EXT456")).thenReturn(Mono.error(new RuntimeException("Database error")));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(customer));
        when(customerAddressRepository.findByCustomerId(anyLong())).thenReturn(Flux.empty());

        // When
        Mono<Void> result = customerService.processBatch(batchId, customers);

        // Then
        StepVerifier.create(result)
            .verifyComplete();

        verify(eventPublisher).publishCustomerCompleted(eq(batchId), eq("EXT123"));
        verify(eventPublisher).publishCustomerFailed(eq(batchId), eq("EXT456"), anyString());
    }

    @Test
    void processBatch_EmptyExternalId_SkipsCustomer() {
        // Given
        String batchId = "BATCH001";
        CustomerDto customer1 = new CustomerDto();
        customer1.setExternalId("EXT123");
        CustomerDto customer2 = new CustomerDto();
        customer2.setExternalId("");  // Empty external ID
        CustomerDto customer3 = new CustomerDto();
        customer3.setExternalId(null);  // Null external ID
        List<CustomerDto> customers = Arrays.asList(customer1, customer2, customer3);

        when(customerMapper.toEntity(customer1)).thenReturn(customer);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);
        when(customerRepository.findByExternalId("EXT123")).thenReturn(Mono.just(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(customer));
        when(customerAddressRepository.findByCustomerId(anyLong())).thenReturn(Flux.empty());

        // When
        Mono<Void> result = customerService.processBatch(batchId, customers);

        // Then
        StepVerifier.create(result)
            .verifyComplete();

        verify(eventPublisher, times(1)).publishCustomerCompleted(eq(batchId), eq("EXT123"));
        verify(eventPublisher, never()).publishCustomerCompleted(eq(batchId), eq(""));
        verify(eventPublisher, never()).publishCustomerCompleted(eq(batchId), eq(null));
    }
}
