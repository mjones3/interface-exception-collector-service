package com.arcone.biopro.distribution.inventory.adapter.in.socket;

import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.inventory.adapter.in.socket.mapper.GetAllAvailableMapper;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.dto.ValidateInventoryOutput;
import com.arcone.biopro.distribution.inventory.application.usecase.UseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

/**
 * Controller for the Validate an Inventory using RSocket.
 */
@Slf4j
@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ValidateInventorySocketServer {

    UseCase<Mono<ValidateInventoryOutput>, InventoryInput> useCase;

    GetAllAvailableMapper mapper;

    @MessageMapping("validateInventory")
    public Mono<InventoryValidationResponseDTO> licensing(InventoryValidationRequest dto) {
        log.info("validateInventory to validate inventory with request: {}", dto.toString());
        return useCase.execute(mapper.toInput(dto))
            .map(mapper::toResponse)
            .doOnSuccess(response -> log.debug("response: {}", response.toString()));
    }

}
