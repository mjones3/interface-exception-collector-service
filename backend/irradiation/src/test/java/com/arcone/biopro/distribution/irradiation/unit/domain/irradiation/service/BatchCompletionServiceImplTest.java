package com.arcone.biopro.distribution.irradiation.unit.domain.irradiation.service;


import com.arcone.biopro.distribution.irradiation.domain.event.IrradiationEventPublisher;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.BatchCompletionServiceImpl;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.*;
import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.ConfigurationKey;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import com.arcone.biopro.distribution.irradiation.domain.service.ProductDeterminationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchCompletionServiceImplTest {

    @Mock
    private BatchRepository batchRepository;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private ProductDeterminationService productDeterminationService;
    @Mock
    private IrradiationEventPublisher eventPublisher;
    @Mock
    private ConfigurationService configurationService;


    private BatchCompletionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BatchCompletionServiceImpl(
                batchRepository,
                deviceRepository,
                productDeterminationService,
                eventPublisher,
                configurationService
        );
    }

    @Test
    void shouldThrowErrorWhenBatchNotFound() {
        // Given
        BatchId batchId = BatchId.of(999L);
        var itemCompletions = List.of(
                new IrradiationAggregate.BatchItemCompletion("W123456789001", "E003300", true)
        );

        when(batchRepository.findById(batchId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(service.prepareBatchCompletion(batchId, itemCompletions, LocalDateTime.now()))
                .expectErrorMatches(error -> 
                    error instanceof RuntimeException && 
                    error.getMessage().equals("Batch not found")
                )
                .verify();
    }

    @Test
    void shouldCompleteBatchWithIrradiatedItems() {
        // Given
        var batch = new Batch(BatchId.of(123L), DeviceId.of("DEV001"), LocalDateTime.now(), null);
        var device = new Device(DeviceId.of("DEV001"), new Location("1FS"), "ACTIVE");
        var itemCompletions = List.of(
                new IrradiationAggregate.BatchItemCompletion("W123456789001", "E003300", true)
        );
        var productDeterminations = Map.of("E003300", 
                new ProductDetermination(1, new ProductCode("E003300"), new ProductCode("E003200"), "IRR CPD LR WB", true));

        var aggregate = new IrradiationAggregate(device, batch, itemCompletions, productDeterminations, LocalDateTime.now());
        var batchItem = BatchItem.builder()
                .unitNumber(new UnitNumber("W123456789001"))
                .productCode("E003300")
                .lotNumber("LOT001")
                .productFamily("PLASMA_TRANSFUSABLE")
                .expirationDate(LocalDateTime.now().plusDays(30))
                .build();

        when(batchRepository.findBatchItem(any(), any(), any())).thenReturn(Mono.just(batchItem));
        when(batchRepository.updateBatchItemNewProductCode(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(batchRepository.completeBatch(any(), any())).thenReturn(Mono.just(batch));
        when(configurationService.readConfiguration(List.of("IRRADIATION_EXPIRATION_DAYS")))
                .thenReturn(Flux.just(new Configuration(new ConfigurationKey("IRRADIATION_EXPIRATION_DAYS"), "28")));

        // When & Then
        StepVerifier.create(service.completeBatch(aggregate))
                .verifyComplete();

        verify(batchRepository).updateBatchItemNewProductCode(BatchId.of(123L), "W123456789001", "E003300", "E003200");
        verify(batchRepository).completeBatch(BatchId.of(123L), aggregate.getCompletionTime());
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldCompleteBatchWithQuarantineItems() {
        // Given
        var batch = new Batch(BatchId.of(123L), DeviceId.of("DEV001"), LocalDateTime.now(), null);
        var device = new Device(DeviceId.of("DEV001"), new Location("1FS"), "ACTIVE");
        var itemCompletions = List.of(
                new IrradiationAggregate.BatchItemCompletion("W123456789001", "E003300", false)
        );

        var aggregate = new IrradiationAggregate(device, batch, itemCompletions, Map.of(), LocalDateTime.now());

        when(batchRepository.completeBatch(any(), any())).thenReturn(Mono.just(batch));

        // When & Then
        StepVerifier.create(service.completeBatch(aggregate))
                .verifyComplete();

        verify(batchRepository).completeBatch(BatchId.of(123L), aggregate.getCompletionTime());
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldHandleEmptyItemCompletions() {
        // Given
        var batch = new Batch(BatchId.of(123L), DeviceId.of("DEV001"), LocalDateTime.now(), null);
        var device = new Device(DeviceId.of("DEV001"), new Location("1FS"), "ACTIVE");
        var aggregate = new IrradiationAggregate(device, batch, List.of(), Map.of(), LocalDateTime.now());

        when(batchRepository.completeBatch(any(), any())).thenReturn(Mono.just(batch));

        // When & Then
        StepVerifier.create(service.completeBatch(aggregate))
                .verifyComplete();

        verify(batchRepository).completeBatch(BatchId.of(123L), aggregate.getCompletionTime());
        verifyNoInteractions(eventPublisher);
    }
}