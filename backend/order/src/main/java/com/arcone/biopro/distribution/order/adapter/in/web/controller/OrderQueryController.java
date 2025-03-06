package com.arcone.biopro.distribution.order.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderQueryCommandDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderReportDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PageDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderQueryMapper;
import com.arcone.biopro.distribution.order.application.mapper.OrderReportMapper;
import com.arcone.biopro.distribution.order.application.mapper.PageMapper;
import com.arcone.biopro.distribution.order.domain.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderQueryService orderQueryService;
    private final OrderQueryMapper orderQueryMapper;
    private final OrderReportMapper orderReportMapper;
    private final PageMapper pageMapper;

    @QueryMapping
    public Mono<PageDTO<OrderReportDTO>> searchOrders(@Argument OrderQueryCommandDTO orderQueryCommandDTO){
        var command = orderQueryMapper.mapToDomain(orderQueryCommandDTO);
        return orderQueryService.search(command)
            .map(page -> pageMapper.mapToDTO(page, orderReportMapper::mapToDTO));
    }

}
