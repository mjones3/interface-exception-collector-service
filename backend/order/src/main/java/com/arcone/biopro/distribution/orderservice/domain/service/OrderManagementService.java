package com.arcone.biopro.distribution.orderservice.domain.service;

import com.arcone.biopro.distribution.orderservice.application.dto.OrderReceivedEventPayloadDTO;
import reactor.core.publisher.Mono;

public interface OrderManagementService {

    Mono processOrder(OrderReceivedEventPayloadDTO eventDTO);
}
