package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.usecase.CompleteOrderUseCase;
import com.arcone.biopro.distribution.order.domain.event.OrderCompletedEvent;
import com.arcone.biopro.distribution.order.domain.model.CompleteOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig
class CompleteOrderUseCaseTest {

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    private OrderRepository orderRepository;

    private OrderShipmentService orderShipmentService;
    private LookupService lookupService;

    private CompleteOrderUseCase useCase;

    @BeforeEach
    public void setup() {
        orderRepository = Mockito.mock(OrderRepository.class);
        orderShipmentService = Mockito.mock(OrderShipmentService.class);
        lookupService = Mockito.mock(LookupService.class);
        useCase = new CompleteOrderUseCase(orderRepository, applicationEventPublisher ,orderShipmentService,lookupService);
    }

    @Test
    public void shouldNotCompleteOrderWhenOrderDoesNotExist() {
        Mockito.when(orderRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.completeOrder(new CompleteOrderCommand(1L,"employeeId","COMMENTS")))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals("COMPLETE_ORDER_ERROR",  detail.notifications().getFirst().useCaseMessageType().name());
                    Assertions.assertEquals("ERROR",  detail.notifications().getFirst().useCaseMessageType().getType().name());
                    Assertions.assertEquals("Cannot complete order",  detail.notifications().getFirst().useCaseMessageType().getMessage());

                }
            )
            .verifyComplete();

        Mockito.verifyNoInteractions(applicationEventPublisher);

    }

    @Test
    public void shouldNotCompleteOrderWhenOrderIsAlreadyClosed() {

        var order = Mockito.mock(Order.class);

        Mockito.when(orderRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(order));

        Mockito.doThrow(new IllegalArgumentException("Order is already completed")).when(order).completeOrder(Mockito.any(CompleteOrderCommand.class),Mockito.any(LookupService.class),Mockito.any(OrderShipmentService.class));

        StepVerifier.create(useCase.completeOrder(new CompleteOrderCommand(1L,"employeeId","COMMENTS")))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals("COMPLETE_ORDER_ERROR",  detail.notifications().getFirst().useCaseMessageType().name());
                    Assertions.assertEquals("ERROR",  detail.notifications().getFirst().useCaseMessageType().getType().name());
                    Assertions.assertEquals("Cannot complete order",  detail.notifications().getFirst().useCaseMessageType().getMessage());

                }
            )
            .verifyComplete();

        Mockito.verifyNoInteractions(applicationEventPublisher);

    }

    @Test
    public void shouldCompleteOrder() {
        var order = Mockito.mock(Order.class);

        Mockito.when(orderRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(order));
        Mockito.when(orderRepository.update(Mockito.any(Order.class))).thenReturn(Mono.just(order));

        StepVerifier.create(useCase.completeOrder(new CompleteOrderCommand(1L,"employeeId","COMMENTS")))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals("ORDER_COMPLETED_SUCCESSFULLY",  detail.notifications().getFirst().useCaseMessageType().name());
                    Assertions.assertEquals("SUCCESS",  detail.notifications().getFirst().useCaseMessageType().getType().name());
                    Assertions.assertEquals("Order completed successfully",  detail.notifications().getFirst().useCaseMessageType().getMessage());
                    Assertions.assertNotNull( detail.data());

                }
            )
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(OrderCompletedEvent.class));
    }
}
