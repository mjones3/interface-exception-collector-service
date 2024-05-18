package com.arcone.biopro.distribution.shippingservice.unit.application.usecase;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulFilledResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulfilledDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.application.usecase.OrderFulfilledServiceUseCase;
import com.arcone.biopro.distribution.shippingservice.domain.model.Order;
import com.arcone.biopro.distribution.shippingservice.domain.model.OrderItem;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderStatus;
import com.arcone.biopro.distribution.shippingservice.domain.repository.OrderItemRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderItemFulfilledMessage;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
class OrderFulfilledServiceUseCaseTest {

    @Test
    public void shouldCreateOrderFulfilled(){

        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        OrderItemRepository orderItemRepository = Mockito.mock(OrderItemRepository.class);

        OrderFulfilledServiceUseCase useCase = new OrderFulfilledServiceUseCase(orderRepository,orderItemRepository);

        OrderFulfilledMessage message = Mockito.mock(OrderFulfilledMessage.class);
        Mockito.when(message.orderNumber()).thenReturn(56L);
        Mockito.when(message.status()).thenReturn("OPEN");
        Mockito.when(message.priority()).thenReturn("ASAP");

        OrderItemFulfilledMessage item = Mockito.mock(OrderItemFulfilledMessage.class);
        Mockito.when(item.bloodType()).thenReturn("AP");
        List<OrderItemFulfilledMessage> items = new ArrayList<>();
        items.add(item);

        Mockito.when(message.items()).thenReturn(items);

        Order order = Mockito.mock(Order.class);

        Mockito.when(orderRepository.save(Mockito.any(Order.class))).thenReturn(Mono.just(order));

        Mockito.when(orderItemRepository.saveAll(Mockito.any(org.reactivestreams.Publisher.class))).thenReturn(Flux.empty());

        Mono<Order> orderCreated = useCase.create(message);

        StepVerifier.create(orderCreated)
            .consumeNextWith(orderSaved -> orderSaved.getId())
            .verifyComplete();

    }

    @Test
    public void shouldListOrderFulfilledRequests(){

        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        OrderItemRepository orderItemRepository = Mockito.mock(OrderItemRepository.class);

        Order order = Mockito.mock(Order.class);
        Mockito.when(order.getOrderNumber()).thenReturn(56L);
        Mockito.when(order.getStatus()).thenReturn(OrderStatus.OPEN);
        Mockito.when(order.getPriority()).thenReturn(OrderPriority.ASAP);

        Mockito.when(orderRepository.findAll()).thenReturn(Flux.just(order));

        OrderFulfilledServiceUseCase useCase = new OrderFulfilledServiceUseCase(orderRepository,orderItemRepository);


        Mono<List<OrderFulFilledResponseDTO>> list = useCase.listOrderFulfilledRequests();

        StepVerifier
            .create(list)
            .consumeNextWith(orderList -> {
                assertEquals(orderList.size(), 1);
                assertEquals(Optional.of(56L), Optional.of(orderList.get(0).orderNumber()));
                assertEquals(Optional.of(OrderStatus.OPEN), Optional.of(orderList.get(0).status()));
                assertEquals(Optional.of(OrderPriority.ASAP), Optional.of(orderList.get(0).priority()));
            })
            .verifyComplete();

    }

    @Test
    public void shouldFindOrderFulfilledByNumber(){

        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        OrderItemRepository orderItemRepository = Mockito.mock(OrderItemRepository.class);

        Order order = Mockito.mock(Order.class);
        Mockito.when(order.getId()).thenReturn(1L);
        Mockito.when(order.getOrderNumber()).thenReturn(56L);
        Mockito.when(order.getStatus()).thenReturn(OrderStatus.OPEN);
        Mockito.when(order.getPriority()).thenReturn(OrderPriority.ASAP);

        Mockito.when(orderRepository.findByOrderNumber(1L)).thenReturn(Mono.just(order));


        OrderItem item = Mockito.mock(OrderItem.class);
        Mockito.when(item.getBloodType()).thenReturn(BloodType.AP);

        Mockito.when(orderItemRepository.findAllByOrderId(1L)).thenReturn(Flux.just(item));

        OrderFulfilledServiceUseCase useCase = new OrderFulfilledServiceUseCase(orderRepository,orderItemRepository);


        Mono<OrderFulfilledDetailResponseDTO>  orderDetail = useCase.getOrderByNumber(1L);

        StepVerifier
            .create(orderDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(56L), Optional.of(detail.orderNumber()));
                assertEquals(Optional.of(OrderStatus.OPEN), Optional.of(detail.status()));
                assertEquals(Optional.of(OrderPriority.ASAP), Optional.of(detail.priority()));
                assertEquals(detail.items().size(), 1);
                assertEquals(Optional.of(BloodType.AP), Optional.of(detail.items().get(0).bloodType()));
            })
            .verifyComplete();

    }

}
