package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.util.RecoveredPlasmaShippingServiceMessages;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ValidateInventoryCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.exception.InventoryServiceNotAvailableException;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.CommandMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryRsocketClient implements InventoryService {

    private final RSocketRequester rSocketRequester;
    private final CommandMapper commandMapper;
    private final InventoryMapper inventoryMapper;

    public Mono<InventoryValidation> validateInventory(ValidateInventoryCommand validateInventoryCommand){
        return rSocketRequester
            .route("validateInventory")
            .data(commandMapper.toRequest(validateInventoryCommand))
            .retrieveMono(InventoryValidationResponseDTO.class)
            .map(inventoryMapper::toValidationModel)
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
