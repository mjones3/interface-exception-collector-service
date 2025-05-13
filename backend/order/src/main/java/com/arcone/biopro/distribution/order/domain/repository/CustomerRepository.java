package com.arcone.biopro.distribution.order.domain.repository;

import com.arcone.biopro.distribution.order.domain.model.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository {

    Flux<Customer> getCustomers();

    Mono<Customer> getCustomerByCode(final String code);
}
