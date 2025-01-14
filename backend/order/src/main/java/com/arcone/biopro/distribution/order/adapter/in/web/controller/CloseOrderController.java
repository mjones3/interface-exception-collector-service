package com.arcone.biopro.distribution.order.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.CloseOrderCommandDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderResponseDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderMapper;
import com.arcone.biopro.distribution.order.domain.model.CloseOrderCommand;
import com.arcone.biopro.distribution.order.domain.service.CloseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class CloseOrderController {

    private final CloseOrderService closeOrderService;
    private final OrderMapper orderMapper;

    @MutationMapping("closeOrder")
    public Mono<OrderResponseDTO> closeOrder(@Argument("closeOrderCommandDTO") CloseOrderCommandDTO closeOrderCommandDTO) {
        return closeOrderService.closeOrder(new CloseOrderCommand(closeOrderCommandDTO.orderId()
                , closeOrderCommandDTO.employeeId(), closeOrderCommandDTO.reason(), closeOrderCommandDTO.comments() ))
            .map(orderMapper::mapToDTO);
    }
}
