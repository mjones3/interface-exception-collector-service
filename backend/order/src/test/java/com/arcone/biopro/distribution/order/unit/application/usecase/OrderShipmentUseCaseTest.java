package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEvenPayloadDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderShipmentMapper;
import com.arcone.biopro.distribution.order.application.usecase.OrderShipmentUseCase;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.repository.OrderShipmentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrderShipmentUseCaseTest {

    @Test
    public void shouldProcessShipmentCreatedEvent(){

        var orderShipmentRepository  = Mockito.mock(OrderShipmentRepository.class);

        var orderShipmentMapper = new OrderShipmentMapper();

        var orderRepository =  Mockito.mock(OrderRepository.class) ;

        var target = new OrderShipmentUseCase(orderShipmentRepository, orderShipmentMapper, orderRepository);

        var orderShipmentMock = Mockito.mock(OrderShipment.class);

        var order = Mockito.mock(Order.class);

        var orderStatus = Mockito.mock(OrderStatus.class);

        Mockito.when(order.getOrderStatus()).thenReturn(orderStatus);

        Mockito.when(orderRepository.findOneByOrderNumber(Mockito.any())).thenReturn(Mono.just(order));

        Mockito.when(orderShipmentRepository.insert(Mockito.any())).thenReturn(Mono.just(orderShipmentMock));

        Mockito.when(orderRepository.update(Mockito.any())).thenReturn(Mono.just(order));

        var response = target.processShipmentCreatedEvent(ShipmentCreatedEvenPayloadDTO
            .builder()
            .orderNumber(1L)
            .shipmentStatus("OPEN")
            .shipmentId(1L)
            .build());

        StepVerifier.create(response)
            .expectNext(orderShipmentMock)
            .verifyComplete();

    }

    @Test
    public void shouldFindOneByOrderNumber(){

        var orderShipmentRepository  = Mockito.mock(OrderShipmentRepository.class);

        var orderShipmentMapper = new OrderShipmentMapper();

        var orderShipmentMock = Mockito.mock(OrderShipment.class);

        var orderRepository =  Mockito.mock(OrderRepository.class) ;

        Mockito.when(orderShipmentRepository.findOneByOrderId(Mockito.any())).thenReturn(Mono.just(orderShipmentMock));

        var target = new OrderShipmentUseCase(orderShipmentRepository, orderShipmentMapper, orderRepository);

        StepVerifier.create(target.findOneByOrderId(1L))
            .expectNext(orderShipmentMock)
            .verifyComplete();
    }


}
