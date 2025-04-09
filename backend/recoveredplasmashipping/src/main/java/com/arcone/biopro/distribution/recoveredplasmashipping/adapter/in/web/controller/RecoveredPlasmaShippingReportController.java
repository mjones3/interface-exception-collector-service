package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.PageDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentQueryCommandRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentReportDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RecoveredPlasmaShipmentReportService;
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
public class RecoveredPlasmaShippingReportController {

    private final UseCaseResponseMapper useCaseResponseDtoMapper;
    private final CommandRequestDTOMapper commandRequestDTOMapper;
    private final RecoveredPlasmaShipmentReportService recoveredPlasmaShipmentReportService;

    @QueryMapping("searchShipment")
    public Mono<UseCaseResponseDTO<PageDTO<RecoveredPlasmaShipmentReportDTO>>> searchShipment(@Argument("recoveredPlasmaShipmentQueryCommandRequestDTO") RecoveredPlasmaShipmentQueryCommandRequestDTO recoveredPlasmaShipmentQueryCommandRequestDTO) {
        log.debug("Request to search Shipment: {}", recoveredPlasmaShipmentQueryCommandRequestDTO);
        return recoveredPlasmaShipmentReportService.search(commandRequestDTOMapper.toInputCommand(recoveredPlasmaShipmentQueryCommandRequestDTO))
            .map(useCaseResponseDtoMapper::toUseCaseRecoveredPlasmaShipmentReportDTO);
    }
}
