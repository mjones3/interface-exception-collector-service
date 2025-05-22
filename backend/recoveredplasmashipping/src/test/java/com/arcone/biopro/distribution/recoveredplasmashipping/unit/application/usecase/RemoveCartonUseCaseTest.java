package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RemoveCartonCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.RemoveCartonUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonRemovedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveCartonUseCaseTest {

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Mock
    private RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CartonRepository cartonRepository;

    @Mock
    private CartonItemRepository cartonItemRepository;

    @InjectMocks
    private RemoveCartonUseCase removeCartonUseCase;

    @Test
    void shouldSuccessfullyRemoveCarton() {
        // Arrange
        Long cartonId = 123L;
        String employeeId = "emp123";
        Long shipmentId = 123L;

        RemoveCartonCommandInput commandInput = new RemoveCartonCommandInput(cartonId, employeeId);
        Carton carton = mock(Carton.class);
        RecoveredPlasmaShipment shipment = mock(RecoveredPlasmaShipment.class);

        RecoveredPlasmaShipmentOutput shipmentOutput = mock(RecoveredPlasmaShipmentOutput.class);

        when(carton.getId()).thenReturn(cartonId);
        when(carton.getShipmentId()).thenReturn(shipmentId);
        when(carton.removeCarton(any(), any())).thenReturn(carton);
        when(shipment.getId()).thenReturn(shipmentId);
        when(shipment.getLocationCode()).thenReturn("LOCATION_CODE");
        when(shipment.markAsReopen()).thenReturn(shipment);

        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));
        when(cartonRepository.update(carton)).thenReturn(Mono.just(carton));
        when(cartonItemRepository.deleteAllByCartonId(cartonId)).thenReturn(Mono.empty());
        when(recoveredPlasmaShippingRepository.findOneById(shipmentId)).thenReturn(Mono.just(shipment));
        when(recoveredPlasmaShippingRepository.update(shipment)).thenReturn(Mono.just(shipment));
        when(recoveredPlasmaShipmentOutputMapper.toRecoveredPlasmaShipmentOutput(shipment)).thenReturn(shipmentOutput);

        // Act
        StepVerifier.create(removeCartonUseCase.removeCarton(commandInput))
            // Assert
            .assertNext(output -> {
                assertNotNull(output);
                Assertions.assertEquals(shipmentOutput, output.data());
                Assertions.assertEquals(1, output.notifications().size());
                Assertions.assertEquals(UseCaseMessageType.CARTON_REMOVED_SUCCESS.getCode(), output.notifications().get(0).useCaseMessage().code());

                verify(applicationEventPublisher).publishEvent(any(RecoveredPlasmaCartonRemovedEvent.class));
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleCartonNotFound() {
        // Arrange
        Long cartonId = 123L;
        String employeeId = "emp123";
        RemoveCartonCommandInput commandInput = new RemoveCartonCommandInput(cartonId, employeeId);

        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(removeCartonUseCase.removeCarton(commandInput))
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                Assertions.assertEquals(1, output.notifications().size());
                Assertions.assertEquals(UseCaseMessageType.CARTON_REMOVED_ERROR.getCode(), output.notifications().get(0).useCaseMessage().code());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleUpdateError() {
        // Arrange
        Long cartonId = 123L;
        String employeeId = "emp123";
        RemoveCartonCommandInput commandInput = new RemoveCartonCommandInput(cartonId, employeeId);
        Carton carton = mock(Carton.class);

        when(carton.removeCarton(any(), any())).thenReturn(carton);
        when(cartonRepository.findOneById(cartonId)).thenReturn(Mono.just(carton));
        when(cartonRepository.update(carton)).thenReturn(Mono.error(new RuntimeException("Update failed")));

        // Act & Assert
        StepVerifier.create(removeCartonUseCase.removeCarton(commandInput))
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                Assertions.assertEquals(1, output.notifications().size());
                Assertions.assertEquals(UseCaseMessageType.CARTON_REMOVED_ERROR.getCode(), output.notifications().get(0).useCaseMessage().code());
            })
            .verifyComplete();
    }
}
