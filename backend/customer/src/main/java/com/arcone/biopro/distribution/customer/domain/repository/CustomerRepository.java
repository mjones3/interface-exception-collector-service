package com.arcone.biopro.distribution.customer.domain.repository;

import com.arcone.biopro.distribution.customer.domain.model.Customer;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends R2dbcRepository<Customer, Long> {
    Mono<Customer> findByExternalId(String externalId);
}
