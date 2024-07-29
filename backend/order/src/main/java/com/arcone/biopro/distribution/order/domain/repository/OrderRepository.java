package com.arcone.biopro.distribution.order.domain.repository;

import com.arcone.biopro.distribution.order.domain.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository {

    Mono<Boolean> existsById(final Long id);

    Mono<Boolean> existsById(final Long id, final Boolean active);

    Flux<Order> findAll();

    Mono<Order> findOneById(final Long id);

    Mono<Order> insert(final Order order);

}
