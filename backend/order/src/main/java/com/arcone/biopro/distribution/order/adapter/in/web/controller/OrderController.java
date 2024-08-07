package com.arcone.biopro.distribution.order.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderMapper;
import com.arcone.biopro.distribution.order.domain.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @QueryMapping
    public Flux<OrderDTO> findAllOrders() {
        return orderService.findAll()
            .map(orderMapper::mapToDTO)
            .flatMap(Mono::just);
    }

    @MutationMapping
    public Mono<OrderDTO> insertOrder(@Argument("order") OrderDTO orderDTO) {
        return orderService.insert(orderMapper.mapToDomain(orderDTO))
            .map(orderMapper::mapToDTO)
            .flatMap(Mono::just);
    }

}
