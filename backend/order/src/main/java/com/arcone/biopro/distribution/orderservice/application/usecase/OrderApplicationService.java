package com.arcone.biopro.distribution.orderservice.application.usecase;

import com.arcone.biopro.distribution.orderservice.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OrderApplicationService implements OrderManagementService {

    @Override
    public Mono processOrder(OrderReceivedEventDTO eventDTO) {
        log.info("Processing Order Received Event {}", eventDTO);
        return Mono.empty();
    }
}
