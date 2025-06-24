package com.arcone.biopro.distribution.eventbridge.domain.service;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCancelledPayload;
import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCreatedEventDTO;
import com.arcone.biopro.distribution.eventbridge.application.dto.OrderModifiedPayload;
import com.arcone.biopro.distribution.eventbridge.application.dto.OrderRejectedPayload;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<Void> processOrderCancelledEvent(OrderCancelledPayload orderPayload);
    Mono<Void> processOrderCreatedEvent(OrderCreatedEventDTO orderEvent);
    Mono<Void> processOrderModifiedEvent(OrderModifiedPayload orderPayload);
    Mono<Void> processOrderRejectedEvent(OrderRejectedPayload orderPayload);
}
