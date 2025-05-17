package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase.RecoveredPlasmaShipmentProcessingUseCase;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentClosedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentProcessingEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Inventory;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryNotification;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.UnacceptableUnitReportRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private RecoveredPlasmaShipmentProcessingUseCase useCase;



        @BeforeEach
        void setUp() {
            useCase = new RecoveredPlasmaShipmentProcessingUseCase(
                recoveredPlasmaShippingRepository,
                inventoryService,
                cartonItemRepository,
                unacceptableUnitReportRepository,
                cartonRepository,
                applicationEventPublisher
            );
        }

        @Test
        void shouldProcessShipmentSuccessfullyWithInventoryValidation() {
            // Given
            Long shipmentId = 123L;
            RecoveredPlasmaShipment shipmentMock = Mockito.mock(RecoveredPlasmaShipment.class);
            when(shipmentMock.getId()).thenReturn(shipmentId);

            RecoveredPlasmaShipmentProcessingEvent event = new RecoveredPlasmaShipmentProcessingEvent(shipmentMock);

            CartonItem cartonItem = createCartonItem();
            InventoryValidation inventoryValidation = createInventoryValidation("EXPIRED");

            when(recoveredPlasmaShippingRepository.findOneById(shipmentId)).thenReturn(Mono.just(shipmentMock));
            when(unacceptableUnitReportRepository.deleteAllByShipmentId(shipmentId)).thenReturn(Mono.empty());
            when(cartonItemRepository.findAllByShipmentId(shipmentId)).thenReturn(Flux.just(cartonItem));
            when(inventoryService.validateInventoryBatch(any())).thenReturn(Flux.just(inventoryValidation));
            when(recoveredPlasmaShippingRepository.update(any())).thenReturn(Mono.just(shipmentMock));
            when(cartonItemRepository.findOneByShipmentIdAndProduct(Mockito.anyLong(),Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(cartonItem));

            var reportItem = Mockito.mock(UnacceptableUnitReportItem.class, RETURNS_DEEP_STUBS);

            when(unacceptableUnitReportRepository.save(Mockito.any())).thenReturn(Mono.just(reportItem));

            var carton = Mockito.mock(Carton.class, RETURNS_DEEP_STUBS);
            when(carton.getCartonNumber()).thenReturn("ABC123");
            when(carton.getCartonSequence()).thenReturn(1);




            when(cartonRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(carton));
            when(cartonRepository.update(Mockito.any())).thenReturn(Mono.just(carton));

            // When
            Mono<RecoveredPlasmaShipment> result = useCase.onRecoveredPlasmaShipmentProcessing(event);

            // Then
            StepVerifier.create(result)
                .expectNext(shipmentMock)
                .verifyComplete();

            verify(recoveredPlasmaShippingRepository,times(1)).findOneById(shipmentId);
            verify(unacceptableUnitReportRepository).deleteAllByShipmentId(shipmentId);
            verify(cartonItemRepository).findAllByShipmentId(shipmentId);
            verify(inventoryService).validateInventoryBatch(any());
            verify(recoveredPlasmaShippingRepository,times(1)).update(any());
        }

    @Test
    void shouldProcessShipmentSuccessfullyWithInventoryValidationPacked() {
        // Given
        Long shipmentId = 123L;
        RecoveredPlasmaShipment shipmentMock = Mockito.mock(RecoveredPlasmaShipment.class);
        when(shipmentMock.getId()).thenReturn(shipmentId);

        RecoveredPlasmaShipmentProcessingEvent event = new RecoveredPlasmaShipmentProcessingEvent(shipmentMock);

        CartonItem cartonItem = Mockito.mock(CartonItem.class);

        InventoryValidation inventoryValidation = Mockito.mock(InventoryValidation.class, RETURNS_DEEP_STUBS);
        InventoryNotification notification = Mockito.mock(InventoryNotification.class , RETURNS_DEEP_STUBS);
        when(notification.getErrorName()).thenReturn("INVENTORY_IS_PACKED");
        when(inventoryValidation.getFirstNotification()).thenReturn(notification);



        when(recoveredPlasmaShippingRepository.findOneById(shipmentId)).thenReturn(Mono.just(shipmentMock));
        when(unacceptableUnitReportRepository.deleteAllByShipmentId(shipmentId)).thenReturn(Mono.empty());
        when(cartonItemRepository.findAllByShipmentId(shipmentId)).thenReturn(Flux.just(cartonItem));

        when(inventoryService.validateInventoryBatch(any())).thenReturn(Flux.just(inventoryValidation));
        when(recoveredPlasmaShippingRepository.update(any())).thenReturn(Mono.just(shipmentMock));

        // When
        Mono<RecoveredPlasmaShipment> result = useCase.onRecoveredPlasmaShipmentProcessing(event);

        // Then
        StepVerifier.create(result)
            .expectNext(shipmentMock)
            .verifyComplete();

        verify(recoveredPlasmaShippingRepository,times(1)).findOneById(shipmentId);
        verify(unacceptableUnitReportRepository).deleteAllByShipmentId(shipmentId);
        verify(cartonItemRepository).findAllByShipmentId(shipmentId);
        verify(inventoryService).validateInventoryBatch(any());
        verify(unacceptableUnitReportRepository,times(0)).save(Mockito.any());
        verify(recoveredPlasmaShippingRepository,times(1)).update(any());
        verify(cartonItemRepository,times(0)).findOneByShipmentIdAndProduct(Mockito.anyLong(),Mockito.anyString(),Mockito.anyString());
    }

    @Test
    void shouldProcessShipmentSuccessfullyAndCloseWhenNoInventoryIssue() {
        // Given
        Long shipmentId = 123L;
        RecoveredPlasmaShipment shipmentMock = Mockito.mock(RecoveredPlasmaShipment.class);
        when(shipmentMock.getId()).thenReturn(shipmentId);

        RecoveredPlasmaShipmentProcessingEvent event = new RecoveredPlasmaShipmentProcessingEvent(shipmentMock);

        CartonItem cartonItem = Mockito.mock(CartonItem.class);

        when(recoveredPlasmaShippingRepository.findOneById(shipmentId)).thenReturn(Mono.just(shipmentMock));
        when(unacceptableUnitReportRepository.deleteAllByShipmentId(shipmentId)).thenReturn(Mono.empty());
        when(cartonItemRepository.findAllByShipmentId(shipmentId)).thenReturn(Flux.just(cartonItem));

        when(inventoryService.validateInventoryBatch(any())).thenReturn(Flux.empty());

        RecoveredPlasmaShipment shipmentUpdateMock = Mockito.mock(RecoveredPlasmaShipment.class);
        when(shipmentUpdateMock.getStatus()).thenReturn("CLOSED");

        Mockito.when(shipmentMock.completeProcessing(Mockito.any())).thenReturn(shipmentUpdateMock);

        when(recoveredPlasmaShippingRepository.update(any())).thenReturn(Mono.just(shipmentUpdateMock));

        // When
        Mono<RecoveredPlasmaShipment> result = useCase.onRecoveredPlasmaShipmentProcessing(event);

        // Then
        StepVerifier.create(result)
            .expectNext(shipmentUpdateMock)
            .verifyComplete();

        verify(recoveredPlasmaShippingRepository,times(1)).findOneById(shipmentId);
        verify(unacceptableUnitReportRepository).deleteAllByShipmentId(shipmentId);
        verify(cartonItemRepository).findAllByShipmentId(shipmentId);
        verify(inventoryService).validateInventoryBatch(any());
        verify(unacceptableUnitReportRepository,times(0)).save(Mockito.any());
        verify(recoveredPlasmaShippingRepository,times(1)).update(any());
        verify(cartonItemRepository,times(0)).findOneByShipmentIdAndProduct(Mockito.anyLong(),Mockito.anyString(),Mockito.anyString());
        verify(applicationEventPublisher,times(1)).publishEvent(Mockito.any(RecoveredPlasmaShipmentClosedEvent.class));
    }

        @Test
        void shouldHandleShipmentNotFound() {
            // Given
            Long shipmentId = 1L;
            RecoveredPlasmaShipment shipmentMock = Mockito.mock(RecoveredPlasmaShipment.class);
            when(shipmentMock.getId()).thenReturn(shipmentId);

            RecoveredPlasmaShipmentProcessingEvent event = new RecoveredPlasmaShipmentProcessingEvent(shipmentMock);

            when(recoveredPlasmaShippingRepository.findOneById(shipmentId))
                .thenReturn(Mono.empty());

            // When
            Mono<RecoveredPlasmaShipment> result = useCase.onRecoveredPlasmaShipmentProcessing(event);

            // Then
            StepVerifier.create(result)
                .verifyComplete();
        }

        @Test
        void shouldHandleProcessingError() {
            // Given
            Long shipmentId = 456L;

            var mockShipment = Mockito.mock(RecoveredPlasmaShipment.class);
            when(mockShipment.getId()).thenReturn(shipmentId);

            RecoveredPlasmaShipmentProcessingEvent event = new RecoveredPlasmaShipmentProcessingEvent(mockShipment);
            RuntimeException processingError = new RuntimeException("Processing failed");

            when(recoveredPlasmaShippingRepository.findOneById(shipmentId)).thenReturn(Mono.just(mockShipment));
            when(unacceptableUnitReportRepository.deleteAllByShipmentId(shipmentId)).thenReturn(Mono.error(processingError));
            when(recoveredPlasmaShippingRepository.update(any())).thenReturn(Mono.just(mockShipment));

            // When
            Mono<RecoveredPlasmaShipment> result = useCase.onRecoveredPlasmaShipmentProcessing(event);

            // Then
            StepVerifier.create(result)
                .expectNext(mockShipment)
                .verifyComplete();

            verify(recoveredPlasmaShippingRepository).update(any());
        }

        // Helper methods to create test objects
        private RecoveredPlasmaShipmentProcessingEvent createProcessingEvent(Long shipmentId) {
            return new RecoveredPlasmaShipmentProcessingEvent(createShipment(shipmentId));
        }

        private RecoveredPlasmaShipment createShipment(Long shipmentId) {
            RecoveredPlasmaShipment recoveredPlasmaShipment = Mockito.mock(RecoveredPlasmaShipment.class);
            when(recoveredPlasmaShipment.getId()).thenReturn(shipmentId);
            when(recoveredPlasmaShipment.getStatus()).thenReturn("PROCESSING");
            when(recoveredPlasmaShipment.getLocationCode()).thenReturn("LOC123");

            return recoveredPlasmaShipment;
        }

        private CartonItem createCartonItem() {
            CartonItem cartonItem = Mockito.mock(CartonItem.class, RETURNS_DEEP_STUBS);
            when(cartonItem.getUnitNumber()).thenReturn("UNIT123");
            when(cartonItem.getProductCode()).thenReturn("PROD123");
           return cartonItem;

        }

        private InventoryValidation createInventoryValidation(String errorName) {
            InventoryValidation inventoryValidation = Mockito.mock(InventoryValidation.class, RETURNS_DEEP_STUBS);
            Inventory inventory = Mockito.mock(Inventory.class , RETURNS_DEEP_STUBS);
            when(inventory.getUnitNumber()).thenReturn("UNIT123");
            when(inventory.getProductCode()).thenReturn("PROD123");

            when(inventoryValidation.getInventory()).thenReturn(inventory);

            InventoryNotification notification = Mockito.mock(InventoryNotification.class , RETURNS_DEEP_STUBS);
            when(notification.getErrorName()).thenReturn(errorName);
            when(notification.getErrorMessage()).thenReturn("Error message");
            when(inventoryValidation.getFirstNotification()).thenReturn(notification);

           return  inventoryValidation;
        }

}

