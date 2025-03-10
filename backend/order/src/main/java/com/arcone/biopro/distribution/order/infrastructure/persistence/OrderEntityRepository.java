package com.arcone.biopro.distribution.order.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;

public interface OrderEntityRepository extends ReactiveCrudRepository<OrderEntity, Long>, ReactiveSortingRepository<OrderEntity, Long> {
}
