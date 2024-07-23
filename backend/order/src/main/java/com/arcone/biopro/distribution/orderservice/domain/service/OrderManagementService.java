package com.arcone.biopro.distribution.orderservice.domain.service;

import com.arcone.biopro.distribution.orderservice.application.dto.OrderReceivedEventDTO;
import reactor.core.publisher.Mono;

public interface OrderManagementService {

    Mono processOrder(OrderReceivedEventDTO eventDTO);
}
