package com.arcone.biopro.distribution.order.domain.repository;

import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository {

    Mono<Boolean> existsById(final Long id, final Boolean active);

    Mono<Order> findOneById(final Long id);

    Mono<Order> insert(final Order order);

    Mono<Order> findOneByOrderNumber(final Long number);

    Mono<Order> update(final Order order);

    Flux<Order> findByExternalId(final String externalId);

    Mono<Page<OrderReport>> search(OrderQueryCommand orderQueryCommand);

}
