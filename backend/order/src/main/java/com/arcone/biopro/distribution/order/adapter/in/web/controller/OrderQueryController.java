package com.arcone.biopro.distribution.order.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderQueryCommandDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderReportDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderQueryMapper;
import com.arcone.biopro.distribution.order.domain.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderQueryService orderQueryService;
    private final OrderQueryMapper orderQueryMapper;

    @QueryMapping
    public Flux<OrderReportDTO> searchOrders(@Argument OrderQueryCommandDTO orderQueryCommandDTO){
        return orderQueryService.searchOrders(orderQueryMapper.mapToDomain(orderQueryCommandDTO))
            .map(orderQueryMapper::mapToDTO);
    }
}
