package com.arcone.biopro.distribution.recoveredplasmashipping.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller.RecoveredPlasmaShippingReportController;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.PageDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentQueryCommandRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentReportDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PageOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentQueryCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RecoveredPlasmaShipmentReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


@ExtendWith(MockitoExtension.class)
class RecoveredPlasmaShippingReportControllerTest {

    @Mock
    private UseCaseResponseMapper useCaseResponseDtoMapper;

    @Mock
    private CommandRequestDTOMapper commandRequestDTOMapper;

    @Mock
    private RecoveredPlasmaShipmentReportService recoveredPlasmaShipmentReportService;

    @InjectMocks
    private RecoveredPlasmaShippingReportController controller;

    @Test
    void shouldSearchShipmentSuccessfully() {
        // Given
        RecoveredPlasmaShipmentQueryCommandRequestDTO requestDTO = createRequestDTO();
        RecoveredPlasmaShipmentQueryCommandInput command = createCommand();
        UseCaseOutput<PageOutput<RecoveredPlasmaShipmentReportOutput>> servicePage = createServicePage();
        UseCaseResponseDTO<PageDTO<RecoveredPlasmaShipmentReportDTO>> expectedResponse = createExpectedResponse();

        Mockito.when(commandRequestDTOMapper.toInputCommand(requestDTO)).thenReturn(command);
        Mockito.when(recoveredPlasmaShipmentReportService.search(command)).thenReturn(Mono.just(servicePage));
        Mockito.when(useCaseResponseDtoMapper.toUseCaseRecoveredPlasmaShipmentReportDTO(servicePage)).thenReturn(expectedResponse);

        // When
        StepVerifier.create(controller.searchShipment(requestDTO))
            // Then
            .expectNext(expectedResponse)
            .verifyComplete();

        Mockito.verify(commandRequestDTOMapper).toInputCommand(requestDTO);
        Mockito.verify(recoveredPlasmaShipmentReportService).search(command);
        Mockito.verify(useCaseResponseDtoMapper).toUseCaseRecoveredPlasmaShipmentReportDTO(servicePage);
    }

    @Test
    void shouldHandleEmptySearchResults() {
        // Given
        RecoveredPlasmaShipmentQueryCommandRequestDTO requestDTO = createRequestDTO();
        RecoveredPlasmaShipmentQueryCommandInput command = createCommand();
        UseCaseOutput<PageOutput<RecoveredPlasmaShipmentReportOutput>> emptyPage = createServicePage();
        UseCaseResponseDTO<PageDTO<RecoveredPlasmaShipmentReportDTO>> emptyResponse = createEmptyResponse();

        Mockito.when(commandRequestDTOMapper.toInputCommand(requestDTO)).thenReturn(command);
        Mockito.when(recoveredPlasmaShipmentReportService.search(command)).thenReturn(Mono.just(emptyPage));
        Mockito.when(useCaseResponseDtoMapper.toUseCaseRecoveredPlasmaShipmentReportDTO(emptyPage)).thenReturn(emptyResponse);

        // When
        StepVerifier.create(controller.searchShipment(requestDTO))
            // Then
            .expectNext(emptyResponse)
            .verifyComplete();
    }

    @Test
    void shouldHandleErrorFromService() {
        // Given
        RecoveredPlasmaShipmentQueryCommandRequestDTO requestDTO = createRequestDTO();
        RecoveredPlasmaShipmentQueryCommandInput command = createCommand();

        Mockito.when(commandRequestDTOMapper.toInputCommand(requestDTO)).thenReturn(command);
        Mockito.when(recoveredPlasmaShipmentReportService.search(command)).thenReturn(Mono.error(new RuntimeException("Service error")));

        // When
        StepVerifier.create(controller.searchShipment(requestDTO))
            // Then
            .expectError(RuntimeException.class)
            .verify();
    }

    private RecoveredPlasmaShipmentQueryCommandRequestDTO createRequestDTO() {
        return RecoveredPlasmaShipmentQueryCommandRequestDTO
            .builder()
            .locationCode(List.of("LOC1"))
        .shipmentNumber("SHIP001")
        .shipmentStatus(List.of("PENDING"))
        .customers(List.of("CUST1"))
        .productTypes(List.of("TYPE1"))
        .shipmentDateFrom(LocalDate.now().minusDays(7))
        .shipmentDateTo(LocalDate.now())
        .pageNumber(0)
        .pageSize(10)
            .build();
    }

    private RecoveredPlasmaShipmentQueryCommandInput createCommand() {
        return new  RecoveredPlasmaShipmentQueryCommandInput(List.of("LOC1"),"SHIP001"
            , List.of("PENDING"), List.of("CUST1"), List.of("TYPE1"), LocalDate.now().minusDays(7),LocalDate.now(), null
            , 0 ,10, null);
    }

    private UseCaseOutput<PageOutput<RecoveredPlasmaShipmentReportOutput>> createServicePage() {
        PageOutput<RecoveredPlasmaShipmentReportOutput> pageOutput = new PageOutput<>(List.of(RecoveredPlasmaShipmentReportOutput.builder().build()),0,10,1,null);
        return new UseCaseOutput<>(Collections.emptyList(),pageOutput,null);
    }

    private UseCaseResponseDTO<PageDTO<RecoveredPlasmaShipmentReportDTO>> createExpectedResponse() {
        PageDTO<RecoveredPlasmaShipmentReportDTO> pageDTO = new PageDTO<>(List.of(RecoveredPlasmaShipmentReportDTO.builder().build()),0,10,1,null);

        UseCaseResponseDTO<PageDTO<RecoveredPlasmaShipmentReportDTO>> response = new UseCaseResponseDTO<>(Collections.emptyList(),pageDTO,null);

        return response;
    }

    private UseCaseResponseDTO<PageDTO<RecoveredPlasmaShipmentReportDTO>> createEmptyResponse() {
        PageDTO<RecoveredPlasmaShipmentReportDTO> pageDTO = new PageDTO<>(Collections.emptyList(),0,10,0,null);
        UseCaseResponseDTO<PageDTO<RecoveredPlasmaShipmentReportDTO>> response = new UseCaseResponseDTO<>(Collections.emptyList(),pageDTO,null);
        return response;
    }
}

