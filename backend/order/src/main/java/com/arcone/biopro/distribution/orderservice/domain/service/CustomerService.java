package com.arcone.biopro.distribution.orderservice.domain.service;

import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    Flux<CustomerDTO> getCustomers();

    Mono<CustomerDTO> getCustomerByCode(final String code);

}
