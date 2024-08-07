package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.order.domain.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

    Flux<Order> findAll();

    Mono<Order> findOneById(final Long id);

    Mono<Order> insert(Order order);

    Mono processOrder(OrderReceivedEventPayloadDTO eventDTO);

}
