package com.arcone.biopro.distribution.customer.domain.repository;

import com.arcone.biopro.distribution.customer.domain.model.CustomerAddress;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface CustomerAddressRepository extends R2dbcRepository<CustomerAddress, Long> {
    Flux<CustomerAddress> findByCustomerId(Long customerId);
}
