package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.order.domain.model.CompleteOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.Order;
import reactor.core.publisher.Mono;

public interface CloseOrderService {

    Mono<UseCaseResponseDTO<Order>> completeOrder(CompleteOrderCommand completeOrderCommand);
}
