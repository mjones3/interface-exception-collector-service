package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.CancelOrderReceivedDTO;
import com.arcone.biopro.distribution.order.application.dto.CancelOrderReceivedPayloadDTO;
import com.arcone.biopro.distribution.order.application.usecase.CancelOrderUseCase;
import com.arcone.biopro.distribution.order.domain.event.OrderCancelledEvent;
import com.arcone.biopro.distribution.order.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.order.domain.model.CancelOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringJUnitConfig
class CancelOrderUseCaseTest {

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    private OrderRepository orderRepository;
    private CancelOrderUseCase useCase;

    @BeforeEach
    public void setUp(){
        orderRepository = Mockito.mock(OrderRepository.class);
        useCase = new CancelOrderUseCase(orderRepository, applicationEventPublisher);
    }

    @Test
    void shouldProcessCancelOrderAndRejectWhenOrderDoesNotExist(){

        Mockito.when(orderRepository.findByExternalId(Mockito.any(String.class))).thenReturn(Flux.empty());

        var event = Mockito.mock(CancelOrderReceivedDTO.class);
        Mockito.when(event.payload()).thenReturn(CancelOrderReceivedPayloadDTO.builder()
                .externalId("externalId")
            .build());


        StepVerifier.create(useCase.processCancelOrderReceivedEvent(event))
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(OrderRejectedEvent.class));
    }

    @Test
    void shouldProcessCancelOrderAndRejectWhenOrderCancelRequestIsInvalid(){

        var order = Mockito.mock(Order.class);
        Mockito.when(order.cancel(Mockito.any(CancelOrderCommand.class),Mockito.anyList())).thenReturn(order);

        Mockito.when(orderRepository.findByExternalId(Mockito.any(String.class))).thenReturn(Flux.just(order));

        var event = Mockito.mock(CancelOrderReceivedDTO.class);
        Mockito.when(event.payload()).thenReturn(CancelOrderReceivedPayloadDTO.builder()
            .externalId("externalId")
            .cancelDate("INVALID_DATE")
            .cancelReason("Reason")
            .cancelEmployeeCode("employee-id")
            .build());


        StepVerifier.create(useCase.processCancelOrderReceivedEvent(event))
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(OrderRejectedEvent.class));
    }

    @Test
    void shouldProcessCancelOrder(){

        var order = Mockito.mock(Order.class);
        Mockito.when(order.cancel(Mockito.any(CancelOrderCommand.class),Mockito.anyList())).thenReturn(order);

        Mockito.when(orderRepository.findByExternalId(Mockito.any(String.class))).thenReturn(Flux.just(order));

        Mockito.when(orderRepository.update(Mockito.any(Order.class))).thenReturn(Mono.just(order));

        var event = Mockito.mock(CancelOrderReceivedDTO.class);
        Mockito.when(event.payload()).thenReturn(CancelOrderReceivedPayloadDTO.builder()
            .externalId("externalId")
                .cancelDate("2025-01-01 00:00:00")
                .cancelReason("Reason")
                .cancelEmployeeCode("employee-id")
                .transactionId(UUID.randomUUID())
            .build());


        StepVerifier.create(useCase.processCancelOrderReceivedEvent(event))
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(OrderCancelledEvent.class));
    }
}
