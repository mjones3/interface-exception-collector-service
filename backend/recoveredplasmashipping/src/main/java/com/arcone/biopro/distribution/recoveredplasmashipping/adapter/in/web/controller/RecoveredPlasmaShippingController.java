package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CreateShipmentRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CreateShipmentRequestDtoMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CreateShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RecoveredPlasmaShippingController {
    private final CreateShipmentService createShipmentService;
    private final CreateShipmentRequestDtoMapper createShipmentRequestMapper;
    private final UseCaseResponseMapper useCaseResponseDtoMapper;

    @MutationMapping("createShipment")
    public Mono<UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>> createShipment(@Argument("createShipmentRequest") CreateShipmentRequestDTO createShipmentRequestDTO) {
        log.debug("Request to Create Shipment: {}", createShipmentRequestDTO);
        return createShipmentService.createShipment(createShipmentRequestMapper.toInput(createShipmentRequestDTO))
            .map(useCaseResponseDtoMapper::toUseCaseRecoveredPlasmaShipmentResponseDTO);
    }
}
