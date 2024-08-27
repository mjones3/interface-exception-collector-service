package com.arcone.biopro.distribution.order.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderShipmentDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderShipmentMapper;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class OrderShipmentController {

    private final OrderShipmentService orderShipmentService;
    private final OrderShipmentMapper orderShipmentMapper;

    @QueryMapping
    public Mono<OrderShipmentDTO> findOrderShipmentByOrderId(@Argument Long orderId) {
        return orderShipmentService.findOneByOrderId(orderId)
            .map(orderShipmentMapper::mapToDto);

    }


}
