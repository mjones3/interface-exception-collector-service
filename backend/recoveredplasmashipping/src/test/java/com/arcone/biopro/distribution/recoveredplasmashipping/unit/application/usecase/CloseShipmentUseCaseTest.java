package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CloseShipmentCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.CloseShipmentUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentProcessingEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CloseShipmentCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseShipmentUseCaseTest {

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CloseShipmentUseCase closeShipmentUseCase;

    @BeforeEach
    void setUp() {
        closeShipmentUseCase = new CloseShipmentUseCase(
            recoveredPlasmaShippingRepository,
            recoveredPlasmaShipmentOutputMapper,
            applicationEventPublisher
        );
    }

    @Test
    void closeShipment_Success() {
        // Arrange
        Long shipmentId = 1L;
        String employeeId = "EMP-001";
        String locationCode = "LOC-001";
        LocalDate shipDate = LocalDate.now();

        CloseShipmentCommandInput commandInput = new CloseShipmentCommandInput(
            shipmentId, employeeId, locationCode, shipDate
        );

        RecoveredPlasmaShipment mockShipment = mock(RecoveredPlasmaShipment.class);
        RecoveredPlasmaShipment updatedShipment = mock(RecoveredPlasmaShipment.class);
        RecoveredPlasmaShipmentOutput mockOutput = mock(RecoveredPlasmaShipmentOutput.class);

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
            .thenReturn(Mono.just(mockShipment));
        when(mockShipment.markAsProcessing(any(CloseShipmentCommand.class)))
            .thenReturn(updatedShipment);
        when(recoveredPlasmaShippingRepository.update(updatedShipment))
            .thenReturn(Mono.just(updatedShipment));
        when(recoveredPlasmaShipmentOutputMapper.toRecoveredPlasmaShipmentOutput(updatedShipment))
            .thenReturn(mockOutput);

        // Act & Assert
        StepVerifier.create(closeShipmentUseCase.closeShipment(commandInput))
            .expectNextMatches(output -> {
                // Verify the output structure
                Assertions.assertEquals(1, output.notifications().size());
                Assertions.assertSame(output.notifications().get(0).useCaseMessage().type(), UseCaseMessageType.SHIPMENT_PROCESSING_SUCCESS.getType());
                Assertions.assertSame(output.data(), mockOutput);
                return true;
            })
            .verifyComplete();

        // Verify interactions
        verify(applicationEventPublisher).publishEvent(any(RecoveredPlasmaShipmentProcessingEvent.class));
        verify(recoveredPlasmaShippingRepository).findOneById(shipmentId);
        verify(recoveredPlasmaShippingRepository).update(updatedShipment);
    }

    @Test
    void closeShipment_ShipmentNotFound() {
        // Arrange
        Long shipmentId = 2L;
        CloseShipmentCommandInput commandInput = new CloseShipmentCommandInput(
            shipmentId, "EMP-001", "LOC-001", LocalDate.now()
        );

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(closeShipmentUseCase.closeShipment(commandInput))
            .expectNextMatches(output -> {
                // Verify error output structure
                Assertions.assertEquals(1, output.notifications().size());
                Assertions.assertSame(output.notifications().get(0).useCaseMessage().type(), UseCaseNotificationType.WARN);
                Assertions.assertEquals(16, (int) output.notifications().get(0).useCaseMessage().code());
                Assertions.assertNull(output.data());
                return true;
            })
            .verifyComplete();

        // Verify no event was published
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void closeShipment_UpdateError() {
        // Arrange
        Long shipmentId = 2L;
        CloseShipmentCommandInput commandInput = new CloseShipmentCommandInput(
            shipmentId, "EMP-001", "LOC-001", LocalDate.now()
        );

        RecoveredPlasmaShipment mockShipment = mock(RecoveredPlasmaShipment.class);
        RecoveredPlasmaShipment updatedShipment = mock(RecoveredPlasmaShipment.class);

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
            .thenReturn(Mono.just(mockShipment));
        when(mockShipment.markAsProcessing(any(CloseShipmentCommand.class)))
            .thenReturn(updatedShipment);
        when(recoveredPlasmaShippingRepository.update(updatedShipment))
            .thenReturn(Mono.error(new RuntimeException("Update failed")));

        // Act & Assert
        StepVerifier.create(closeShipmentUseCase.closeShipment(commandInput))
            .expectNextMatches(output -> {
                // Verify error output structure
                Assertions.assertEquals(1, output.notifications().size());
                Assertions.assertSame(output.notifications().get(0).useCaseMessage().type(), UseCaseNotificationType.WARN);
                Assertions.assertEquals(16, (int) output.notifications().get(0).useCaseMessage().code());
                Assertions.assertNull(output.data());
                return true;
            })
            .verifyComplete();

        // Verify no event was published
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void closeShipment_BusinessError() {
        // Arrange
        Long shipmentId = 2L;
        CloseShipmentCommandInput commandInput = new CloseShipmentCommandInput(
            shipmentId, "EMP-001", "LOC-001", LocalDate.now().minusDays(2)
        );

        RecoveredPlasmaShipment mockShipment = mock(RecoveredPlasmaShipment.class);

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
            .thenReturn(Mono.just(mockShipment));


        // Act & Assert
        StepVerifier.create(closeShipmentUseCase.closeShipment(commandInput))
            .expectNextMatches(output -> {
                // Verify error output structure
                Assertions.assertEquals(1, output.notifications().size());
                Assertions.assertSame(output.notifications().get(0).useCaseMessage().type(), UseCaseNotificationType.WARN);
                Assertions.assertEquals(16, (int) output.notifications().get(0).useCaseMessage().code());
                Assertions.assertNull(output.data());
                Assertions.assertEquals("Ship date cannot be in the past", output.notifications().getFirst().useCaseMessage().message());
                return true;
            })
            .verifyComplete();

        // Verify no event was published
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}

