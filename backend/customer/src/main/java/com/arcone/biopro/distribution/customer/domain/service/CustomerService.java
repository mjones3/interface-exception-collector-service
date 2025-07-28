package com.arcone.biopro.distribution.customer.domain.service;

import com.arcone.biopro.distribution.customer.application.dto.CustomerDto;
import com.arcone.biopro.distribution.customer.application.dto.CustomerResponseDto;
import com.arcone.biopro.distribution.customer.application.mapper.CustomerMapper;
import com.arcone.biopro.distribution.customer.domain.model.Customer;
import com.arcone.biopro.distribution.customer.domain.model.CustomerAddress;
import com.arcone.biopro.distribution.customer.domain.repository.CustomerRepository;
import com.arcone.biopro.distribution.customer.domain.repository.CustomerAddressRepository;
import com.arcone.biopro.distribution.customer.infrastructure.messaging.CustomerEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerMapper customerMapper;
    private final CustomerEventPublisher eventPublisher;

    @Transactional
    public Mono<CustomerResponseDto> upsertCustomer(CustomerDto customerDto) {
        return customerRepository.findByExternalId(customerDto.getExternalId())
            .flatMap(existingCustomer -> updateCustomer(existingCustomer, customerDto)
                .map(dto -> {
                    CustomerResponseDto response = new CustomerResponseDto();
                    response.setId(existingCustomer.getId());
                    response.setExternalId(dto.getExternalId());
                    response.setName(dto.getName());
                    response.setCode(dto.getCode());
                    response.setMessage("Customer updated successfully");
                    response.setCreated(false);
                    return response;
                }))
            .switchIfEmpty(createCustomer(customerDto)
                .flatMap(dto -> customerRepository.findByExternalId(dto.getExternalId())
                    .map(savedCustomer -> {
                        CustomerResponseDto response = new CustomerResponseDto();
                        response.setId(savedCustomer.getId());
                        response.setExternalId(dto.getExternalId());
                        response.setName(dto.getName());
                        response.setCode(dto.getCode());
                        response.setMessage("Customer created successfully");
                        response.setCreated(true);
                        return response;
                    })));
    }

    private Mono<CustomerDto> createCustomer(CustomerDto customerDto) {
        Customer customer = customerMapper.toEntity(customerDto);

        return customerRepository.save(customer)
            .flatMap(savedCustomer -> saveAddresses(savedCustomer, customerDto)
                .then(Mono.just(customerMapper.toDto(savedCustomer))));
    }

    private Mono<CustomerDto> updateCustomer(Customer existingCustomer, CustomerDto customerDto) {
        existingCustomer.setName(customerDto.getName());
        existingCustomer.setCode(customerDto.getCode());
        existingCustomer.setDepartmentCode(customerDto.getDepartmentCode());
        existingCustomer.setDepartmentName(customerDto.getDepartmentName());
        existingCustomer.setPhoneNumber(customerDto.getPhoneNumber());
        existingCustomer.setForeignFlag(customerDto.getForeignFlag());
        existingCustomer.setCustomerType(String.valueOf(customerDto.getType()));
        existingCustomer.setActive(customerDto.getStatus());

        return customerRepository.save(existingCustomer)
            .flatMap(savedCustomer -> customerAddressRepository.findByCustomerId(savedCustomer.getId())
                .flatMap(customerAddressRepository::delete)
                .then(saveAddresses(savedCustomer, customerDto))
                .then(Mono.just(customerMapper.toDto(savedCustomer))));
    }

    private Mono<Void> saveAddresses(Customer customer, CustomerDto customerDto) {
        if (customerDto.getCustomerAddresses() == null || customerDto.getCustomerAddresses().isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(customerDto.getCustomerAddresses())
            .map(addressDto -> {
                CustomerAddress address = customerMapper.toEntity(addressDto);
                address.setCustomerId(customer.getId());
                address.setCreateDate(ZonedDateTime.now());
                address.setModificationDate(ZonedDateTime.now());
                return address;
            })
            .flatMap(customerAddressRepository::save)
            .then();
    }

    public Mono<Void> processBatch(String batchId, List<CustomerDto> customers) {
        return Flux.fromIterable(customers)
            .filter(customerDto -> customerDto.getExternalId() != null && !customerDto.getExternalId().trim().isEmpty())
            .flatMap(customerDto ->
                upsertCustomer(customerDto)
                    .doOnSuccess(response -> eventPublisher.publishCustomerCompleted(batchId, customerDto.getExternalId()))
                    .doOnError(error -> eventPublisher.publishCustomerFailed(batchId, customerDto.getExternalId(), error.getMessage()))
                    .onErrorResume(error -> Mono.empty())
            )
            .then();
    }
}
