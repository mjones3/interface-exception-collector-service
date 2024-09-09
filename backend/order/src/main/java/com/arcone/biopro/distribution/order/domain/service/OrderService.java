package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.order.domain.model.Order;
import reactor.core.publisher.Mono;

public interface OrderService {


    Mono<UseCaseResponseDTO<Order>> findUseCaseResponseById(final Long id);

    Mono<Order> findOneById(final Long id);

    Mono processOrder(OrderReceivedEventPayloadDTO eventDTO);

}
