package com.arcone.biopro.distribution.irradiation.unit.application.irradiation.usecase;

import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.ProductModified;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProduct;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.producer.ProductModifiedProducer;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.producer.QuarantineProductProducer;
import com.arcone.biopro.distribution.irradiation.application.irradiation.command.CompleteBatchCommand;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchItemCompletionDTO;
import com.arcone.biopro.distribution.irradiation.application.irradiation.usecase.CompleteBatchUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import com.arcone.biopro.distribution.irradiation.domain.service.ProductDeterminationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompleteBatchUseCaseTest {

    @Mock
    private BatchRepository batchRepository;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private ProductDeterminationService productDeterminationService;
    @Mock
    private ProductModifiedProducer productModifiedProducer;
    @Mock
    private QuarantineProductProducer quarantineProductProducer;
    @Mock
    private ConfigurationService configurationService;

    private CompleteBatchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CompleteBatchUseCase(
                batchRepository,
                deviceRepository,
                productDeterminationService,
                productModifiedProducer,
                quarantineProductProducer,
                configurationService
        );
    }

    @Test
    void shouldCompleteBatchWithIrradiatedAndNonIrradiatedItems() {
        // Given
        String batchId = "123";
        LocalDateTime endTime = LocalDateTime.now();

        var irradiatedItem = BatchItemCompletionDTO.builder()
                .unitNumber("W123456789001")
                .productCode("E003300")
                .isIrradiated(true)
                .build();

        var nonIrradiatedItem = BatchItemCompletionDTO.builder()
                .unitNumber("W123456789002")
                .productCode("E003301")
                .isIrradiated(false)
                .build();

        var command = CompleteBatchCommand.builder()
                .batchId(batchId)
                .endTime(endTime)
                .batchItems(List.of(irradiatedItem, nonIrradiatedItem))
                .build();

        // Setup mocks
        var batch = new Batch(BatchId.of(123L), DeviceId.of("DEV001"), LocalDateTime.now(), null);
        var device = new Device(DeviceId.of("DEV001"), new Location("1FS"), "ACTIVE");
        var targetProductCode = new ProductCode("E003200");
        var configuration = new Configuration(new com.arcone.biopro.distribution.irradiation.domain.model.vo.ConfigurationKey("IRRADIATION_EXPIRATION_DAYS"), "28");

        when(batchRepository.findById(BatchId.of(123L))).thenReturn(Mono.just(batch));
        when(deviceRepository.findByDeviceId(DeviceId.of("DEV001"))).thenReturn(Mono.just(device));
        when(productDeterminationService.findProductDetermination(any())).thenReturn(Mono.just(
                new ProductDetermination(1, new ProductCode("E003300"), targetProductCode, "IRR CPD LR WB", true)));
        when(batchRepository.findBatchItem(any(), any(), any())).thenReturn(Mono.just(
                com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem.builder()
                        .unitNumber(new com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber("W123456789001"))
                        .productCode("E003300")
                        .lotNumber("LOT001")
                        .productFamily("PLASMA_TRANSFUSABLE")
                        .build()));
        when(batchRepository.updateBatchItemNewProductCode(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(batchRepository.completeBatch(any(), any())).thenReturn(Mono.just(batch));
        when(productModifiedProducer.publishProductModified(any())).thenReturn(Mono.empty());
        when(quarantineProductProducer.publishQuarantineProduct(any())).thenReturn(Mono.empty());
        when(configurationService.readConfiguration(List.of("IRRADIATION_EXPIRATION_DAYS")))
                .thenReturn(Flux.just(configuration));

        // When & Then
        StepVerifier.create(useCase.execute(command))
                .assertNext(result -> {
                    assertThat(result.batchId()).isEqualTo(123L);
                    assertThat(result.message()).isEqualTo("Batch completed successfully");
                    assertThat(result.success()).isTrue();
                })
                .verifyComplete();

        // Verify database updates
        verify(batchRepository).updateBatchItemNewProductCode(
                BatchId.of(123L), "W123456789001", "E003300", "E003200");
        verify(batchRepository).completeBatch(BatchId.of(123L), endTime);

        // Verify Kafka events
        verifyProductModifiedEvent();
        verifyQuarantineEvent();
    }

    @Test
    void shouldHandleOnlyIrradiatedItems() {
        // Given
        var irradiatedItem = BatchItemCompletionDTO.builder()
                .unitNumber("W123456789001")
                .productCode("E003300")
                .isIrradiated(true)
                .build();

        var command = CompleteBatchCommand.builder()
                .batchId("123")
                .endTime(LocalDateTime.now())
                .batchItems(List.of(irradiatedItem))
                .build();

        // Setup mocks
        var batch = new Batch(BatchId.of(123L), DeviceId.of("DEV001"), LocalDateTime.now(), null);
        var device = new Device(DeviceId.of("DEV001"), new Location("1FS"), "ACTIVE");
        var targetProductCode = new ProductCode("E003200");
        var configuration = new Configuration(new com.arcone.biopro.distribution.irradiation.domain.model.vo.ConfigurationKey("IRRADIATION_EXPIRATION_DAYS"), "28");

        when(batchRepository.findById(BatchId.of(123L))).thenReturn(Mono.just(batch));
        when(deviceRepository.findByDeviceId(DeviceId.of("DEV001"))).thenReturn(Mono.just(device));
        when(productDeterminationService.findProductDetermination(any())).thenReturn(Mono.just(
                new ProductDetermination(1, new ProductCode("E003300"), targetProductCode, "IRR CPD LR WB", true)));
        when(batchRepository.findBatchItem(any(), any(), any())).thenReturn(Mono.just(
                com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem.builder()
                        .unitNumber(new com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber("W123456789001"))
                        .productCode("E003300")
                        .lotNumber("LOT001")
                        .productFamily("PLASMA_TRANSFUSABLE")
                        .build()));
        when(batchRepository.updateBatchItemNewProductCode(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(batchRepository.completeBatch(any(), any())).thenReturn(Mono.just(batch));
        when(productModifiedProducer.publishProductModified(any())).thenReturn(Mono.empty());
        when(configurationService.readConfiguration(List.of("IRRADIATION_EXPIRATION_DAYS")))
                .thenReturn(Flux.just(configuration));

        // When & Then
        StepVerifier.create(useCase.execute(command))
                .assertNext(result -> assertThat(result.success()).isTrue())
                .verifyComplete();

        verify(productModifiedProducer).publishProductModified(any());
        verify(quarantineProductProducer, never()).publishQuarantineProduct(any());
    }

    @Test
    void shouldHandleOnlyNonIrradiatedItems() {
        // Given
        var nonIrradiatedItem = BatchItemCompletionDTO.builder()
                .unitNumber("W123456789002")
                .productCode("E003301")
                .isIrradiated(false)
                .build();

        var command = CompleteBatchCommand.builder()
                .batchId("123")
                .endTime(LocalDateTime.now())
                .batchItems(List.of(nonIrradiatedItem))
                .build();

        // Setup mocks
        var batch = new Batch(BatchId.of(123L), DeviceId.of("DEV001"), LocalDateTime.now(), null);
        var device = new Device(DeviceId.of("DEV001"), new Location("1FS"), "ACTIVE");

        when(batchRepository.findById(BatchId.of(123L))).thenReturn(Mono.just(batch));
        when(deviceRepository.findByDeviceId(DeviceId.of("DEV001"))).thenReturn(Mono.just(device));
        when(batchRepository.completeBatch(any(), any())).thenReturn(Mono.just(batch));
        when(quarantineProductProducer.publishQuarantineProduct(any())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(command))
                .assertNext(result -> assertThat(result.success()).isTrue())
                .verifyComplete();

        verify(productModifiedProducer, never()).publishProductModified(any());
        verify(quarantineProductProducer).publishQuarantineProduct(any());
    }

    @Test
    void shouldUseDefaultExpirationDaysWhenConfigurationNotFound() {
        // Given
        var irradiatedItem = BatchItemCompletionDTO.builder()
                .unitNumber("W123456789001")
                .productCode("E003300")
                .isIrradiated(true)
                .build();

        var command = CompleteBatchCommand.builder()
                .batchId("123")
                .endTime(LocalDateTime.now())
                .batchItems(List.of(irradiatedItem))
                .build();

        // Setup mocks
        var batch = new Batch(BatchId.of(123L), DeviceId.of("DEV001"), LocalDateTime.now(), null);
        var device = new Device(DeviceId.of("DEV001"), new Location("1FS"), "ACTIVE");
        var targetProductCode = new ProductCode("E003200");

        when(batchRepository.findById(BatchId.of(123L))).thenReturn(Mono.just(batch));
        when(deviceRepository.findByDeviceId(DeviceId.of("DEV001"))).thenReturn(Mono.just(device));
        when(productDeterminationService.findProductDetermination(any())).thenReturn(Mono.just(
                new ProductDetermination(1, new ProductCode("E003300"), targetProductCode, "IRR CPD LR WB", true)));
        when(batchRepository.findBatchItem(any(), any(), any())).thenReturn(Mono.just(
                com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem.builder()
                        .unitNumber(new com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber("W123456789001"))
                        .productCode("E003300")
                        .lotNumber("LOT001")
                        .productFamily("PLASMA_TRANSFUSABLE")
                        .build()));
        when(batchRepository.updateBatchItemNewProductCode(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(batchRepository.completeBatch(any(), any())).thenReturn(Mono.just(batch));
        when(productModifiedProducer.publishProductModified(any())).thenReturn(Mono.empty());
        when(configurationService.readConfiguration(List.of("IRRADIATION_EXPIRATION_DAYS")))
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(useCase.execute(command))
                .assertNext(result -> assertThat(result.success()).isTrue())
                .verifyComplete();

        verify(productModifiedProducer).publishProductModified(any());
    }



    private void verifyProductModifiedEvent() {
        ArgumentCaptor<ProductModified> captor = ArgumentCaptor.forClass(ProductModified.class);
        verify(productModifiedProducer).publishProductModified(captor.capture());

        ProductModified event = captor.getValue();
        assertThat(event.unitNumber()).isEqualTo("W123456789001");
        assertThat(event.productCode()).isEqualTo("E003200");
        assertThat(event.productDescription()).isEqualTo("IRR CPD LR WB");
        assertThat(event.parentProductCode()).isEqualTo("E003300");
        assertThat(event.productFamily()).isEqualTo("PLASMA_TRANSFUSABLE");
        assertThat(event.expirationTime()).isEqualTo("23:59");
        assertThat(event.modificationLocation()).isEqualTo("1FS");
    }

    private void verifyQuarantineEvent() {
        ArgumentCaptor<QuarantineProduct> captor = ArgumentCaptor.forClass(QuarantineProduct.class);
        verify(quarantineProductProducer).publishQuarantineProduct(captor.capture());

        QuarantineProduct event = captor.getValue();
        assertThat(event.products()).hasSize(1);
        assertThat(event.products().getFirst().unitNumber()).isEqualTo("W123456789002");
        assertThat(event.products().getFirst().productCode()).isEqualTo("E003301");
        assertThat(event.triggeredBy()).isEqualTo("IRRADIATION_SYSTEM");
        assertThat(event.reasonKey()).isEqualTo("IRRADIATION_INCOMPLETE");
        assertThat(event.performedBy()).isEqualTo("IRRADIATION_SYSTEM");
    }

    @Test
    void shouldHandleEmptyBatchItems() {
        var command = CompleteBatchCommand.builder()
                .batchId("123")
                .endTime(LocalDateTime.now())
                .batchItems(Collections.emptyList())
                .build();

        // Setup only the necessary mocks for this test
        var batch = new Batch(BatchId.of(123L), DeviceId.of("DEV001"), LocalDateTime.now(), null);
        var device = new Device(DeviceId.of("DEV001"), new Location("1FS"), "ACTIVE");
        
        when(batchRepository.findById(BatchId.of(123L))).thenReturn(Mono.just(batch));
        when(deviceRepository.findByDeviceId(DeviceId.of("DEV001"))).thenReturn(Mono.just(device));
        when(batchRepository.completeBatch(any(), any())).thenReturn(Mono.just(batch));

        StepVerifier.create(useCase.execute(command))
                .expectNextCount(1)
                .verifyComplete();

        verify(productModifiedProducer, never()).publishProductModified(any());
        verify(quarantineProductProducer, never()).publishQuarantineProduct(any());
    }

    @Test
    void shouldHandleBatchNotFound() {
        var command = CompleteBatchCommand.builder()
                .batchId("999")
                .endTime(LocalDateTime.now())
                .batchItems(List.of())
                .build();

        // Only setup the specific mock needed for this test
        when(batchRepository.findById(BatchId.of(999L))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(command))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldHandleProductDeterminationServiceError() {
        var irradiatedItem = BatchItemCompletionDTO.builder()
                .unitNumber("W123456789001")
                .productCode("INVALID")
                .isIrradiated(true)
                .build();

        var command = CompleteBatchCommand.builder()
                .batchId("123")
                .endTime(LocalDateTime.now())
                .batchItems(List.of(irradiatedItem))
                .build();

        // Setup only the necessary mocks for this test
        var batch = new Batch(BatchId.of(123L), DeviceId.of("DEV001"), LocalDateTime.now(), null);
        var device = new Device(DeviceId.of("DEV001"), new Location("1FS"), "ACTIVE");
        
        when(batchRepository.findById(BatchId.of(123L))).thenReturn(Mono.just(batch));
        when(deviceRepository.findByDeviceId(DeviceId.of("DEV001"))).thenReturn(Mono.just(device));
        when(productDeterminationService.findProductDetermination(any()))
                .thenReturn(Mono.error(new RuntimeException("Product not found")));
        when(batchRepository.completeBatch(any(), any())).thenReturn(Mono.just(batch));

        StepVerifier.create(useCase.execute(command))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldHandleInvalidBatchIdFormat() {
        var command = CompleteBatchCommand.builder()
                .batchId("invalid-id")
                .endTime(LocalDateTime.now())
                .batchItems(List.of())
                .build();

        // No mocks needed - exception happens during parsing
        StepVerifier.create(useCase.execute(command))
                .expectError(RuntimeException.class)
                .verify();
    }
}
