package com.arcone.biopro.distribution.eventbridge.domain.service;

import com.arcone.biopro.distribution.eventbridge.application.dto.*;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<Void> processOrderCancelledEvent(OrderCancelledPayload orderEvent);
    Mono<Void> processOrderCreatedEvent(OrderCreatedEventDTO orderEvent);
    Mono<Void> processOrderModifiedEvent(OrderModifiedEventDTO orderEvent);
    Mono<Void> processOrderRejectedEvent(OrderRejectedPayload orderEvent);
}
