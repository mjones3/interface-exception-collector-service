package com.arcone.biopro.distribution.shippingservice.infrastructure.service;

import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.shippingservice.infrastructure.service.errors.InventoryServiceNotAvailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryRsocketClient {

    private final RSocketRequester rSocketRequester;

    public Mono<InventoryValidationResponseDTO> validateInventory(InventoryValidationRequest request){
        return rSocketRequester
            .route("validateInventory")
            .data(request)
            .retrieveMono(InventoryValidationResponseDTO.class);
    }

    @MessageExceptionHandler(RuntimeException.class)
    public Mono<RuntimeException> exceptionHandler(RuntimeException e) {
        log.error(e.getMessage());
        return Mono.error(new InventoryServiceNotAvailableException("inventory-service-not-available.error"));
    }
}
