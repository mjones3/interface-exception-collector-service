package com.arcone.biopro.distribution.shippingservice.domain.repository;

import com.arcone.biopro.distribution.shippingservice.domain.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

    Mono<Order> findByOrderNumber(Long orderNumber);

}
