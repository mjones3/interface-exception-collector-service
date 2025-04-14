package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.FindShipmentCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentQueryCommandInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.RecoveredPlasmaShipmentUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.FindShipmentCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoveredPlasmaShipmentUseCaseTest {


    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;


    private RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;


    private RecoveredPlasmaShipmentQueryCommandInputMapper recoveredPlasmaShipmentQueryCommandInputMapper;


    private RecoveredPlasmaShipmentUseCase useCase;

    private FindShipmentCommandInput commandInput;
    private FindShipmentCommand command;
    private RecoveredPlasmaShipment shipment;

    @BeforeEach
    void setUp() {
        // Initialize test data

        recoveredPlasmaShipmentOutputMapper = Mappers.getMapper(RecoveredPlasmaShipmentOutputMapper.class);

        recoveredPlasmaShipmentQueryCommandInputMapper = Mappers.getMapper(RecoveredPlasmaShipmentQueryCommandInputMapper.class);
        recoveredPlasmaShippingRepository = Mockito.mock(RecoveredPlasmaShippingRepository.class);


        useCase = new RecoveredPlasmaShipmentUseCase(recoveredPlasmaShippingRepository,recoveredPlasmaShipmentOutputMapper,recoveredPlasmaShipmentQueryCommandInputMapper);

        commandInput = new FindShipmentCommandInput(1L, "LOC123", "EMP456");
        command = new FindShipmentCommand(1L, "LOC123", "EMP456");
        shipment = Mockito.mock(RecoveredPlasmaShipment.class);

    }

    @Test
    void shouldSuccessfullyFindShipmentById() {
        // Given
        Mockito.when(shipment.getLocationCode()).thenReturn("LOC123");

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));

        // When
        Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> result = useCase.findOneById(commandInput);

        // Then
        StepVerifier.create(result)
            .assertNext(useCaseOutput -> {
                assertNotNull(useCaseOutput);
                assertNotNull(useCaseOutput.data());
            })
            .verifyComplete();

        verify(recoveredPlasmaShippingRepository).findOneById(
            command.getShipmentId());
    }

   @Test
    void shouldHandleNotFoundShipment() {
        // Given

       Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        // When
        Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> result = useCase.findOneById(commandInput);

        // Then
       StepVerifier.create(result)
           .assertNext(useCaseOutput -> {
               assertNotNull(useCaseOutput);
               assertNotNull(useCaseOutput.notifications());
               assertNotNull(useCaseOutput.notifications().getFirst());
               assertEquals(UseCaseNotificationType.WARN,useCaseOutput.notifications().getFirst().useCaseMessage().getType());
               assertEquals("Domain not found for key 1",useCaseOutput.notifications().getFirst().useCaseMessage().getMessage());
               assertNull(useCaseOutput.data());
           })
           .verifyComplete();
    }

    @Test
    void shouldHandleRepositoryError() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database error");

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.error(expectedError));


        // When
        Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> result = useCase.findOneById(commandInput);

        // Then
        StepVerifier.create(result)
            .assertNext(useCaseOutput -> {
                assertNotNull(useCaseOutput);
                assertNotNull(useCaseOutput.notifications());
                assertNotNull(useCaseOutput.notifications().getFirst());
                assertEquals(UseCaseNotificationType.WARN,useCaseOutput.notifications().getFirst().useCaseMessage().getType());
                assertEquals(expectedError.getMessage(),useCaseOutput.notifications().getFirst().useCaseMessage().getMessage());
                assertNull(useCaseOutput.data());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleMapperError() {
        // Given
        RuntimeException expectedError = new RuntimeException("Mapping error");

        recoveredPlasmaShipmentOutputMapper = Mockito.mock(RecoveredPlasmaShipmentOutputMapper.class);

        Mockito.when(shipment.getLocationCode()).thenReturn("LOC123");

        useCase = new RecoveredPlasmaShipmentUseCase(recoveredPlasmaShippingRepository,recoveredPlasmaShipmentOutputMapper,recoveredPlasmaShipmentQueryCommandInputMapper);


        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));

        when(recoveredPlasmaShipmentOutputMapper.toRecoveredPlasmaShipmentOutput(shipment)).thenThrow(expectedError);

        // When
        Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> result = useCase.findOneById(commandInput);

        // Then
        StepVerifier.create(result)
            .assertNext(useCaseOutput -> {
                assertNotNull(useCaseOutput);
                assertNotNull(useCaseOutput.notifications());
                assertNotNull(useCaseOutput.notifications().getFirst());
                assertEquals(UseCaseNotificationType.WARN,useCaseOutput.notifications().getFirst().useCaseMessage().getType());
                assertEquals(expectedError.getMessage(),useCaseOutput.notifications().getFirst().useCaseMessage().getMessage());
                assertNull(useCaseOutput.data());
            })
            .verifyComplete();
    }
}

