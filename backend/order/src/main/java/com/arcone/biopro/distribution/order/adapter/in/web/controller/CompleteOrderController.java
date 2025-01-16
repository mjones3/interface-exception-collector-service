package com.arcone.biopro.distribution.order.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.CompleteOrderCommandDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderResponseDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderMapper;
import com.arcone.biopro.distribution.order.domain.model.CompleteOrderCommand;
import com.arcone.biopro.distribution.order.domain.service.CloseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class CompleteOrderController {

    private final CloseOrderService closeOrderService;
    private final OrderMapper orderMapper;

    @MutationMapping("completeOrder")
    public Mono<OrderResponseDTO> completeOrder(@Argument("completeOrderCommandDTO") CompleteOrderCommandDTO completeOrderCommandDTO) {
        return closeOrderService.completeOrder(new CompleteOrderCommand(completeOrderCommandDTO.orderId()
                , completeOrderCommandDTO.employeeId(), completeOrderCommandDTO.comments() ))
            .map(orderMapper::mapToDTO);
    }
}
