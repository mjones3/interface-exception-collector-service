package com.arcone.biopro.distribution.order.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.controller.OrderShipmentController;
import com.arcone.biopro.distribution.order.application.mapper.OrderShipmentMapper;
import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrderShipmentControllerTest {

    @Test
    public void shouldFindOneByOrderId(){
        var service = Mockito.mock(OrderShipmentService.class);

        var mapper = new OrderShipmentMapper();

        var orderShipment  = Mockito.mock(OrderShipment.class);

        Mockito.when(service.findOneByOrderId(Mockito.any())).thenReturn(Mono.just(orderShipment));

        var controller = new OrderShipmentController(service,mapper);

        StepVerifier.create(controller.findOrderShipmentByOrderId(1L))
            .consumeNextWith(Assertions::assertNotNull)
            .verifyComplete();
    }

}
