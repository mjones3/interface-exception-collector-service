package com.arcone.biopro.distribution.shippingservice.domain.service;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulFilledResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulfilledDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.domain.model.Order;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderFulfilledMessage;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderFulfilledService {

    Mono<Order> create(OrderFulfilledMessage message);

    Mono<OrderFulfilledDetailResponseDTO> getOrderByNumber(Long orderNumber);

    Mono<List<OrderFulFilledResponseDTO>> listOrderFulfilledRequests();
}
