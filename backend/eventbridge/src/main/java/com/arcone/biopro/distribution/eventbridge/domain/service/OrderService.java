package com.arcone.biopro.distribution.eventbridge.domain.service;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCancelledPayload;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<Void> processOrderCancelledEvent(OrderCancelledPayload orderCancelledPayload);
}