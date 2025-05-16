package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.RecoveredPlasmaShipmentProcessingUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentProcessingEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryNotification;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.UnacceptableUnitReportRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoveredPlasmaShipmentProcessingUseCaseTest {

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private CartonItemRepository cartonItemRepository;
    @Mock
    private UnacceptableUnitReportRepository unacceptableUnitReportRepository;
    @Mock
    private CartonRepository cartonRepository;

    @InjectMocks
    private RecoveredPlasmaShipmentProcessingUseCase useCase;

    @Test
    void shouldProcessShipmentSuccessfullyWithNoInventoryIssues() {
        // Given
        Long shipmentId = 1L;
        RecoveredPlasmaShipmentProcessingEvent event = createEvent(shipmentId);
        RecoveredPlasmaShipment shipment = createShipment(shipmentId);
        CartonItem cartonItem = createCartonItem(shipmentId);

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId)).thenReturn(Mono.just(shipment));
        when(unacceptableUnitReportRepository.deleteAllByShipmentId(shipmentId)).thenReturn(Mono.empty());
        when(cartonItemRepository.findAllByShipmentId(shipmentId)).thenReturn(Flux.just(cartonItem));

        InventoryValidation inventoryValidation = Mockito.mock(InventoryValidation.class);

        when(inventoryService.validateInventoryBatch(any())).thenReturn(Flux.just(inventoryValidation));
        when(recoveredPlasmaShippingRepository.update(any())).thenReturn(Mono.just(shipment));

        // When
        StepVerifier.create(useCase.onRecoveredPlasmaShipmentProcessing(event))
            // Then
            .expectNext(shipment)
            .verifyComplete();
    }

    @Test
    void shouldProcessShipmentWithInventoryIssues() {
        // Given
        Long shipmentId = 123L;
        RecoveredPlasmaShipmentProcessingEvent event = createEvent(shipmentId);
        RecoveredPlasmaShipment shipment = createShipment(shipmentId);
        CartonItem cartonItem = createCartonItem(shipmentId);
        Carton carton = createCarton();
        InventoryValidation inventoryValidation = createInventoryValidationWithError();

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
            .thenReturn(Mono.just(shipment));
        when(unacceptableUnitReportRepository.deleteAllByShipmentId(shipmentId))
            .thenReturn(Mono.empty());
        when(cartonItemRepository.findAllByShipmentId(shipmentId)).thenReturn(Flux.just(cartonItem));
        when(inventoryService.validateInventoryBatch(any()))
            .thenReturn(Flux.just(inventoryValidation));
        when(cartonRepository.findOneById(any()))
            .thenReturn(Mono.just(carton));
        when(cartonRepository.update(any()))
            .thenReturn(Mono.just(carton));

        UnacceptableUnitReportItem item = Mockito.mock(UnacceptableUnitReportItem.class);
        when(unacceptableUnitReportRepository.save(any())).thenReturn(Mono.just(item));
        when(recoveredPlasmaShippingRepository.update(any()))
            .thenReturn(Mono.just(shipment));

        // When
        StepVerifier.create(useCase.onRecoveredPlasmaShipmentProcessing(event))
            // Then
            .expectNext(shipment)
            .verifyComplete();
    }

    @Test
    void shouldHandleShipmentNotFound() {
        // Given
        Long shipmentId = 123L;
        RecoveredPlasmaShipmentProcessingEvent event = createEvent(shipmentId);

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId)).thenReturn(Mono.empty());

        // When
        StepVerifier.create(useCase.onRecoveredPlasmaShipmentProcessing(event))
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void shouldHandleErrorAndMarkShipmentAsProcessingError() {
        // Given
        Long shipmentId = 123L;
        RecoveredPlasmaShipmentProcessingEvent event = createEvent(shipmentId);
        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(shipment.getId()).thenReturn(shipmentId);

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId)).thenReturn(Mono.just(shipment));
        when(unacceptableUnitReportRepository.deleteAllByShipmentId(shipmentId)).thenReturn(Mono.error(new RuntimeException("Test error")));
        when(recoveredPlasmaShippingRepository.update(any())).thenReturn(Mono.just(shipment));

        // When
        StepVerifier.create(useCase.onRecoveredPlasmaShipmentProcessing(event))
            // Then
            .expectNext(shipment)
            .verifyComplete();
    }

    @Test
    void shouldProcessShipmentSuccessfullyWithInventoryPackedIssue() {

        // Given
        Long shipmentId = 123L;
        RecoveredPlasmaShipmentProcessingEvent event = createEvent(shipmentId);
        RecoveredPlasmaShipment shipment = createShipment(shipmentId);
        CartonItem cartonItem = createCartonItem(shipmentId);


        InventoryValidation inventoryValidation = Mockito.mock(InventoryValidation.class);
        InventoryNotification notification = Mockito.mock(InventoryNotification.class);

        Mockito.when(notification.getErrorName()).thenReturn("INVENTORY_IS_PACKED");

        Mockito.when(inventoryValidation.getFirstNotification()).thenReturn(notification);


        when(recoveredPlasmaShippingRepository.findOneById(shipmentId)).thenReturn(Mono.just(shipment));
        when(unacceptableUnitReportRepository.deleteAllByShipmentId(shipmentId)).thenReturn(Mono.empty());
        when(cartonItemRepository.findAllByShipmentId(shipmentId)).thenReturn(Flux.just(cartonItem));
        when(inventoryService.validateInventoryBatch(any())).thenReturn(Flux.just(inventoryValidation));

        when(recoveredPlasmaShippingRepository.update(any())).thenReturn(Mono.just(shipment));

        // When
        StepVerifier.create(useCase.onRecoveredPlasmaShipmentProcessing(event))
            // Then
            .expectNext(shipment)
            .verifyComplete();

        verify(cartonRepository, never()).findOneById(any());
        verify(cartonRepository, never()).update(any());
        verify(unacceptableUnitReportRepository, never()).save(any());

    }

    // Helper methods to create test objects
    private RecoveredPlasmaShipmentProcessingEvent createEvent(Long shipmentId) {
        RecoveredPlasmaShipment  payload = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(payload.getId()).thenReturn(shipmentId);
        return new RecoveredPlasmaShipmentProcessingEvent(payload);
    }

    private RecoveredPlasmaShipment createShipment(Long shipmentId) {
        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(shipment.getId()).thenReturn(shipmentId);
        Mockito.when(shipment.getLocationCode()).thenReturn("LocationCode");
        return shipment;
    }

    private CartonItem createCartonItem(Long shipmentId) {
        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(cartonItem.getProductCode()).thenReturn("PRODUCT_CODE");
        return cartonItem;
    }

    private Carton createCarton() {
        Carton carton = Mockito.mock(Carton.class);
        Mockito.when(carton.getCartonNumber()).thenReturn("CARTON_NUMBER");
        Mockito.when(carton.getCartonSequence()).thenReturn(1);
        return carton;
    }

    private InventoryValidation createInventoryValidationWithError() {
        InventoryValidation validation = Mockito.mock(InventoryValidation.class);
        InventoryNotification notification = Mockito.mock(InventoryNotification.class);
        Mockito.when(notification.getErrorMessage()).thenReturn("ERROR_MESSAGE");

        Mockito.when(validation.getFirstNotification()).thenReturn(notification);
        return validation;
    }
}

