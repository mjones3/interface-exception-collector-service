package com.arcone.biopro.distribution.eventbridge.domain.service;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderPayload;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<Void> processOrderCancelledEvent(OrderPayload orderPayload);
    Mono<Void> processOrderModifiedEvent(OrderPayload orderPayload);
}