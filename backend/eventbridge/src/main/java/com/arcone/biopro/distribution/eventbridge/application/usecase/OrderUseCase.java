package com.arcone.biopro.distribution.eventbridge.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.OrderMapper;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderCancelledOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderModifiedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderOutbound;
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
    private final OrderMapper orderMapper;

    @Override
    public Mono<Void> processOrderCancelledEvent(OrderPayload orderPayload) {
        return publishOrderCancelledOutboundEvent(orderMapper.toDomain(orderPayload));
    }

    @Override
    public Mono<Void> processOrderModifiedEvent(OrderPayload orderPayload) {
        return publishOrderModifiedOutboundEvent(orderMapper.toDomain(orderPayload));
    }

    private Mono<Void> publishOrderCancelledOutboundEvent(OrderOutbound orderOutbound) {
        applicationEventPublisher.publishEvent(new OrderCancelledOutboundEvent(orderOutbound));
        return Mono.empty();
    }

    private Mono<Void> publishOrderModifiedOutboundEvent(OrderOutbound orderOutbound) {
        applicationEventPublisher.publishEvent(new OrderModifiedOutboundEvent(orderOutbound));
        return Mono.empty();
    }
}