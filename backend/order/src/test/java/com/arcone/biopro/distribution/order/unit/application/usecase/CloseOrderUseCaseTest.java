package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.usecase.CloseOrderUseCase;
import com.arcone.biopro.distribution.order.domain.event.OrderCompletedEvent;
import com.arcone.biopro.distribution.order.domain.model.CloseOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
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
class CloseOrderUseCaseTest {

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    private OrderRepository orderRepository;

    private CloseOrderUseCase useCase;

    @BeforeEach
    public void setup() {
        orderRepository = Mockito.mock(OrderRepository.class);
        useCase = new CloseOrderUseCase(orderRepository, applicationEventPublisher);
    }

    @Test
    public void shouldNotCloseOrderWhenOrderDoesNotExist() {
        Mockito.when(orderRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.closeOrder(new CloseOrderCommand(1L,"employeeId","REASON","COMMENTS")))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals("CLOSE_ORDER_ERROR",  detail.notifications().getFirst().useCaseMessageType().name());
                    Assertions.assertEquals("ERROR",  detail.notifications().getFirst().useCaseMessageType().getType().name());
                    Assertions.assertEquals("Cannot close order",  detail.notifications().getFirst().useCaseMessageType().getMessage());

                }
            )
            .verifyComplete();

        Mockito.verifyNoInteractions(applicationEventPublisher);

    }

    @Test
    public void shouldNotCloseOrderWhenOrderIsAlreadyClosed() {

        var order = Mockito.mock(Order.class);

        Mockito.when(orderRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(order));

        Mockito.doThrow(new IllegalArgumentException("Order is already closed")).when(order).closeOrder(Mockito.any(CloseOrderCommand.class));

        StepVerifier.create(useCase.closeOrder(new CloseOrderCommand(1L,"employeeId","REASON","COMMENTS")))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals("CLOSE_ORDER_ERROR",  detail.notifications().getFirst().useCaseMessageType().name());
                    Assertions.assertEquals("ERROR",  detail.notifications().getFirst().useCaseMessageType().getType().name());
                    Assertions.assertEquals("Cannot close order",  detail.notifications().getFirst().useCaseMessageType().getMessage());

                }
            )
            .verifyComplete();

        Mockito.verifyNoInteractions(applicationEventPublisher);

    }

    @Test
    public void shouldCloseOrder() {
        var order = Mockito.mock(Order.class);

        Mockito.when(orderRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(order));
        Mockito.when(orderRepository.update(Mockito.any(Order.class))).thenReturn(Mono.just(order));

        StepVerifier.create(useCase.closeOrder(new CloseOrderCommand(1L,"employeeId","REASON","COMMENTS")))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals("ORDER_CLOSED_SUCCESSFULLY",  detail.notifications().getFirst().useCaseMessageType().name());
                    Assertions.assertEquals("SUCCESS",  detail.notifications().getFirst().useCaseMessageType().getType().name());
                    Assertions.assertEquals("Order completed successfully",  detail.notifications().getFirst().useCaseMessageType().getMessage());
                    Assertions.assertNotNull( detail.data());

                }
            )
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(OrderCompletedEvent.class));
    }
}
