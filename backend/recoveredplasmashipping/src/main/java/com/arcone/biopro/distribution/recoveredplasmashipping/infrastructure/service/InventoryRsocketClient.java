package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.util.RecoveredPlasmaShippingServiceMessages;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.exception.InventoryServiceNotAvailableException;
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
            .retrieveMono(InventoryValidationResponseDTO.class)
            .onErrorResume(error -> {
                log.error("Error On Validate inventory {}",error.getMessage());
                   return  Mono.error(new InventoryServiceNotAvailableException(RecoveredPlasmaShippingServiceMessages.INVENTORY_SERVICE_NOT_AVAILABLE_ERROR));
                }
          );
    }

    @MessageExceptionHandler(RuntimeException.class)
    public Mono<RuntimeException> exceptionHandler(RuntimeException e) {
        log.error(e.getMessage());
        return Mono.error(new InventoryServiceNotAvailableException(RecoveredPlasmaShippingServiceMessages.INVENTORY_SERVICE_NOT_AVAILABLE_ERROR));
    }
}
