package com.arcone.biopro.distribution.eventbridge.unit.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCancelledPayload;
import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCreatedEventDTO;
import com.arcone.biopro.distribution.eventbridge.application.dto.OrderModifiedEventDTO;
import com.arcone.biopro.distribution.eventbridge.application.dto.OrderRejectedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.OrderCancelledMapper;
import com.arcone.biopro.distribution.eventbridge.application.mapper.OrderCreatedMapper;
import com.arcone.biopro.distribution.eventbridge.application.mapper.OrderModifiedMapper;
import com.arcone.biopro.distribution.eventbridge.application.mapper.OrderRejectedMapper;
import com.arcone.biopro.distribution.eventbridge.application.usecase.OrderUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import reactor.test.StepVerifier;

class OrderUseCaseTest {

    private ApplicationEventPublisher applicationEventPublisher;
    private OrderCancelledMapper orderCancelledMapper;
    private OrderCreatedMapper orderCreatedMapper;
    private OrderModifiedMapper orderModifiedMapper;
    private OrderRejectedMapper orderRejectedMapper;

    @BeforeEach
    public void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        orderCancelledMapper = Mockito.mock(OrderCancelledMapper.class);
        orderCreatedMapper = Mockito.mock(OrderCreatedMapper.class);
        orderModifiedMapper = Mockito.mock(OrderModifiedMapper.class);
        orderRejectedMapper = Mockito.mock(OrderRejectedMapper.class);
    }

    @Test
    public void shouldProcessOrderCancelledEvent() {
        var mockPayload = Mockito.mock(OrderCancelledPayload.class);
        var target = new OrderUseCase(applicationEventPublisher, orderCancelledMapper, orderCreatedMapper, orderModifiedMapper, orderRejectedMapper);

        StepVerifier
            .create(target.processOrderCancelledEvent(mockPayload))
            .verifyComplete();
    }

    @Test
    public void shouldProcessOrderCreatedEvent() {
        var mockPayload = Mockito.mock(OrderCreatedEventDTO.class);
        var target = new OrderUseCase(applicationEventPublisher, orderCancelledMapper, orderCreatedMapper, orderModifiedMapper, orderRejectedMapper);

        StepVerifier
            .create(target.processOrderCreatedEvent(mockPayload))
            .verifyComplete();
    }

    @Test
    public void shouldProcessOrderModifiedEvent() {
        var mockPayload = Mockito.mock(OrderModifiedEventDTO.class);
        var target = new OrderUseCase(applicationEventPublisher, orderCancelledMapper, orderCreatedMapper, orderModifiedMapper, orderRejectedMapper);

        StepVerifier
            .create(target.processOrderModifiedEvent(mockPayload))
            .verifyComplete();
    }

    @Test
    public void shouldProcessOrderRejectedEvent() {
        var mockPayload = Mockito.mock(OrderRejectedPayload.class);
        var target = new OrderUseCase(applicationEventPublisher, orderCancelledMapper, orderCreatedMapper, orderModifiedMapper, orderRejectedMapper);

        StepVerifier
            .create(target.processOrderRejectedEvent(mockPayload))
            .verifyComplete();
    }
}