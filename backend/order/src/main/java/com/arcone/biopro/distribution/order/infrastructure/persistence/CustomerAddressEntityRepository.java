package com.arcone.biopro.distribution.order.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CustomerAddressEntityRepository extends ReactiveCrudRepository<CustomerAddressEntity, Long> {

    Flux<CustomerAddressEntity> findAllByCustomerIdAndActiveTrue(Long customerId);

}
