package com.arcone.biopro.distribution.orderservice.application.usecase;

import com.arcone.biopro.distribution.orderservice.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.orderservice.domain.event.OrderCreatedEvent;
import com.arcone.biopro.distribution.orderservice.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderApplicationService implements OrderManagementService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Mono processOrder(OrderReceivedEventPayloadDTO eventDTO) {
        log.info("Processing Order Received Event {}", eventDTO);
        return Mono.empty();
    }

    private Mono<Void> publishOrderCreatedEvent(){
        applicationEventPublisher.publishEvent(new OrderCreatedEvent());
        return Mono.empty();
    }

    private Mono<Void> publishOrderRejectedEvent(){
        applicationEventPublisher.publishEvent(new OrderRejectedEvent());
        return Mono.empty();
    }
}
