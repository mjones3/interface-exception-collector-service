package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CloseShipmentRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CreateShipmentRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.FindShipmentRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.PrintUnacceptableUnitReportRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UnacceptableUnitReportDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CreateShipmentRequestDtoMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CloseShipmentService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CreateShipmentService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RecoveredPlasmaShipmentService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.UnacceptableUnitReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RecoveredPlasmaShippingController {
    private final CreateShipmentService createShipmentService;
    private final RecoveredPlasmaShipmentService recoveredPlasmaShipmentService;
    private final CreateShipmentRequestDtoMapper createShipmentRequestMapper;
    private final CommandRequestDTOMapper commandRequestDTOMapper;
    private final UseCaseResponseMapper useCaseResponseDtoMapper;
    private final CloseShipmentService closeShipmentService;
    private final UnacceptableUnitReportService unacceptableUnitReportService;

    @MutationMapping("createShipment")
    public Mono<UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>> createShipment(@Argument("createShipmentRequest") CreateShipmentRequestDTO createShipmentRequestDTO) {
        log.debug("Request to Create Shipment: {}", createShipmentRequestDTO);
        return createShipmentService.createShipment(createShipmentRequestMapper.toInput(createShipmentRequestDTO))
            .map(useCaseResponseDtoMapper::toUseCaseRecoveredPlasmaShipmentResponseDTO);
    }

    @QueryMapping("findShipmentById")
    public Mono<UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>> findShipmentById(@Argument("findShipmentCommandDTO") FindShipmentRequestDTO findShipmentRequestDTO) {
        log.debug("Request to find Shipment : {}", findShipmentRequestDTO);
        return recoveredPlasmaShipmentService.findOneById(commandRequestDTOMapper.toInputCommand(findShipmentRequestDTO))
            .map(useCaseResponseDtoMapper::toUseCaseRecoveredPlasmaShipmentResponseDTO);
    }

    @MutationMapping("closeShipment")
    public Mono<UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>> closeShipment(@Argument("closeShipmentRequest") CloseShipmentRequestDTO closeShipmentRequestDTO) {
        log.debug("Request to Close Shipment: {}", closeShipmentRequestDTO);
        return closeShipmentService.closeShipment(commandRequestDTOMapper.toInputCommand(closeShipmentRequestDTO))
            .map(useCaseResponseDtoMapper::toUseCaseRecoveredPlasmaShipmentResponseDTO);
    }

    @QueryMapping("printUnacceptableUnitsReport")
    public Mono<UseCaseResponseDTO<UnacceptableUnitReportDTO>> printUnacceptableUnitsReport(@Argument("printUnacceptableUnitReportRequest") PrintUnacceptableUnitReportRequestDTO printUnacceptableUnitReportRequestDTO) {
        log.debug("Request to print Unacceptable Units Report for Shipment : {}", printUnacceptableUnitReportRequestDTO);
        return unacceptableUnitReportService.printUnacceptableUnitReport(commandRequestDTOMapper.toInputCommand(printUnacceptableUnitReportRequestDTO))
            .map(useCaseResponseDtoMapper::toUseCaseUnacceptableUnitReportDTO);
    }
}
