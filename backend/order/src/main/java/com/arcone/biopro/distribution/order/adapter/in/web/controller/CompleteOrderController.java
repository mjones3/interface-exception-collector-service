package com.arcone.biopro.distribution.order.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.CompleteOrderCommandDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderResponseDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderMapper;
import com.arcone.biopro.distribution.order.domain.model.CompleteOrderCommand;
import com.arcone.biopro.distribution.order.domain.service.CompleteOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CompleteOrderController {

    private final CompleteOrderService completeOrderService;
    private final OrderMapper orderMapper;

    @MutationMapping("completeOrder")
    public Mono<OrderResponseDTO> completeOrder(@Argument("completeOrderCommandDTO") CompleteOrderCommandDTO completeOrderCommandDTO) {
      log.debug("Request to completeOrder {}", completeOrderCommandDTO);
        return completeOrderService.completeOrder(new CompleteOrderCommand(completeOrderCommandDTO.orderId()
                , completeOrderCommandDTO.employeeId(), completeOrderCommandDTO.comments() ))
            .map(orderMapper::mapToDTO);
    }
}
