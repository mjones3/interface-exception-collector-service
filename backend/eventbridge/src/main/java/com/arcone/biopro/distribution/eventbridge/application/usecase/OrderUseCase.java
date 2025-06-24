package com.arcone.biopro.distribution.eventbridge.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCancelledPayload;
import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCreatedPayload;
import com.arcone.biopro.distribution.eventbridge.application.dto.OrderModifiedPayload;
import com.arcone.biopro.distribution.eventbridge.application.dto.OrderRejectedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.OrderCancelledMapper;
import com.arcone.biopro.distribution.eventbridge.application.mapper.OrderCreatedMapper;
import com.arcone.biopro.distribution.eventbridge.application.mapper.OrderModifiedMapper;
import com.arcone.biopro.distribution.eventbridge.application.mapper.OrderRejectedMapper;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderCancelledOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderCreatedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderModifiedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderRejectedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCancelledOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCreatedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderModifiedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderRejectedOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderUseCase implements OrderService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrderCancelledMapper orderCancelledMapper;
    private final OrderCreatedMapper orderCreatedMapper;
    private final OrderModifiedMapper orderModifiedMapper;
    private final OrderRejectedMapper orderRejectedMapper;

    @Override
    public Mono<Void> processOrderCancelledEvent(OrderCancelledPayload orderPayload) {
        return publishOrderCancelledOutboundEvent(orderCancelledMapper.toDomain(orderPayload));
    }

    @Override
    public Mono<Void> processOrderCreatedEvent(OrderCreatedPayload orderPayload) {
        return publishOrderCreatedOutboundEvent(orderCreatedMapper.toDomain(orderPayload));
    }

    @Override
    public Mono<Void> processOrderModifiedEvent(OrderModifiedPayload orderPayload) {
        return publishOrderModifiedOutboundEvent(orderModifiedMapper.toDomain(orderPayload));
    }

    @Override
    public Mono<Void> processOrderRejectedEvent(OrderRejectedPayload orderPayload) {
        return publishOrderRejectedOutboundEvent(orderRejectedMapper.toDomain(orderPayload));
    }

    private Mono<Void> publishOrderCancelledOutboundEvent(OrderCancelledOutbound orderCancelledOutbound) {
        applicationEventPublisher.publishEvent(new OrderCancelledOutboundEvent(orderCancelledOutbound));
        return Mono.empty();
    }

    private Mono<Void> publishOrderCreatedOutboundEvent(OrderCreatedOutbound orderCreatedOutbound) {
        applicationEventPublisher.publishEvent(new OrderCreatedOutboundEvent(orderCreatedOutbound));
        return Mono.empty();
    }

    private Mono<Void> publishOrderModifiedOutboundEvent(OrderModifiedOutbound orderModifiedOutbound) {
        applicationEventPublisher.publishEvent(new OrderModifiedOutboundEvent(orderModifiedOutbound));
        return Mono.empty();
    }

    private Mono<Void> publishOrderRejectedOutboundEvent(OrderRejectedOutbound orderRejectedOutbound) {
        applicationEventPublisher.publishEvent(new OrderRejectedOutboundEvent(orderRejectedOutbound));
        return Mono.empty();
    }
}
