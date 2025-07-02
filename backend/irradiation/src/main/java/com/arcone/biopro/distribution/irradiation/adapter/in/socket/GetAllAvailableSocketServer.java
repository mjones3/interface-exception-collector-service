package com.arcone.biopro.distribution.irradiation.adapter.in.socket;

import com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto.GetAvailableInventoryCommandDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto.GetAvailableInventoryResponseDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.socket.mapper.GetAllAvailableMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.GetAllAvailableInventoriesInput;
import com.arcone.biopro.distribution.irradiation.application.dto.GetAllAvailableInventoriesOutput;
import com.arcone.biopro.distribution.irradiation.application.usecase.UseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

/**
 * Controller for the Get Available Inventories using RSocket.
 */
@Slf4j
@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GetAllAvailableSocketServer {

    UseCase<Mono<GetAllAvailableInventoriesOutput>, GetAllAvailableInventoriesInput> useCase;

    GetAllAvailableMapper mapper;

    @MessageMapping("getAvailableInventoryWithShortDatedProducts")
    public Mono<GetAvailableInventoryResponseDTO> getAllAvailableInventoryByCriteria(GetAvailableInventoryCommandDTO getAllAvailableRequestDTO) {
        log.info("getAvailableInventoryWithShortDatedProducts to get all available inventories with request: {}", getAllAvailableRequestDTO.toString());
        return useCase.execute(mapper.toInput(getAllAvailableRequestDTO))
            .map(mapper::toResponse)
            .doOnSuccess(response -> log.debug("response: {}", response.toString()));
    }

}
