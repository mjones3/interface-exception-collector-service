package com.arcone.biopro.distribution.eventbridge.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCancelledPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.OrderMapper;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderCancelledOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCancelledOutbound;
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
    public Mono<Void> processOrderCancelledEvent(OrderCancelledPayload orderCancelledPayload) {
        return publishOrderCancelledOutboundEvent(orderMapper.toDomain(orderCancelledPayload));
    }

    private Mono<Void> publishOrderCancelledOutboundEvent(OrderCancelledOutbound orderCancelledOutbound) {
        applicationEventPublisher.publishEvent(new OrderCancelledOutboundEvent(orderCancelledOutbound));
        return Mono.empty();
    }
}