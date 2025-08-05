package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.CartonCreatedUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonCreatedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CartonCreatedUseCaseTest {


    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    private CartonCreatedUseCase cartonCreatedUseCase;

    @BeforeEach
    void setUp() {
        cartonCreatedUseCase = new CartonCreatedUseCase(recoveredPlasmaShippingRepository);
    }

    @Test
    void handleCartonCreatedEvent_Success() {
        // Arrange
        Long shipmentId = 1L;
        RecoveredPlasmaCartonCreatedEvent event = Mockito.mock(RecoveredPlasmaCartonCreatedEvent.class);
        Carton carton = Mockito.mock(Carton.class);
        Mockito.when(carton.getShipmentId()).thenReturn(shipmentId);
        Mockito.when(event.getPayload()).thenReturn(carton);
        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);
        RecoveredPlasmaShipment updatedShipment = Mockito.mock(RecoveredPlasmaShipment.class);

        Mockito.when(shipment.markAsInProgress()).thenReturn(updatedShipment);

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(shipmentId)).thenReturn(Mono.just(shipment));
        Mockito.when(recoveredPlasmaShippingRepository.update(Mockito.any(RecoveredPlasmaShipment.class))).thenReturn(Mono.just(updatedShipment));

        // Act

        StepVerifier.create(cartonCreatedUseCase.handleCartonCreatedEvent(event))
            .expectNextCount(1)
            .verifyComplete();


        // Assert
        Mockito.verify(recoveredPlasmaShippingRepository).findOneById(shipmentId);
        Mockito.verify(recoveredPlasmaShippingRepository).update(Mockito.any(RecoveredPlasmaShipment.class));
    }

    @Test
    void handleCartonCreatedEvent_WhenFindOneByIdFails() {
        // Arrange
        Long shipmentId = 1L;
        RecoveredPlasmaCartonCreatedEvent event = Mockito.mock(RecoveredPlasmaCartonCreatedEvent.class);
        Carton carton = Mockito.mock(Carton.class);
        Mockito.when(carton.getShipmentId()).thenReturn(shipmentId);
        Mockito.when(event.getPayload()).thenReturn(carton);
        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
            .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act
        StepVerifier.create(cartonCreatedUseCase.handleCartonCreatedEvent(event))
            .verifyComplete();

        // Assert
        Mockito.verify(recoveredPlasmaShippingRepository).findOneById(shipmentId);
        Mockito.verify(recoveredPlasmaShippingRepository, Mockito.never()).update(Mockito.any(RecoveredPlasmaShipment.class));
    }

    @Test
    void handleCartonCreatedEvent_WhenMarkInProgressFails() {
        // Arrange
        Long shipmentId = 1L;
        RecoveredPlasmaCartonCreatedEvent event = Mockito.mock(RecoveredPlasmaCartonCreatedEvent.class);
        Carton carton = Mockito.mock(Carton.class);
        Mockito.when(carton.getShipmentId()).thenReturn(shipmentId);
        Mockito.when(event.getPayload()).thenReturn(carton);
        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);

        Mockito.when(shipment.markAsInProgress()).thenThrow(new IllegalArgumentException("Error Test"));

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
            .thenReturn(Mono.just(shipment));

        // Act
        StepVerifier.create(cartonCreatedUseCase.handleCartonCreatedEvent(event))
            .verifyComplete();

        // Assert
        Mockito.verify(recoveredPlasmaShippingRepository).findOneById(shipmentId);
        Mockito.verify(recoveredPlasmaShippingRepository, Mockito.never()).update(Mockito.any(RecoveredPlasmaShipment.class));
    }



}
