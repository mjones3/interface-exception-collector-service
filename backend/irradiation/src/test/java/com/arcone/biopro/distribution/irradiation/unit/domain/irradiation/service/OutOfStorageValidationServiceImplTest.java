package com.arcone.biopro.distribution.irradiation.unit.domain.irradiation.service;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.OutOfStorageValidationService.ProcessingResult;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.OutOfStorageValidationServiceImpl;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.ConfigurationKey;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutOfStorageValidationServiceImplTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private OutOfStorageValidationServiceImpl outOfStorageValidationService;

    @Test
    void processProductStoredEvent_WhenWithinTimeLimit_ShouldReturnNotQuarantine() {
        // Given
        String unitNumber = "W036825008001";
        String productCode = "E003300";
        String deviceUse = "Collection";
        ZonedDateTime storageTime = ZonedDateTime.now();
        ZonedDateTime batchStartTime = storageTime.minusMinutes(20);

        Batch batch = new Batch(
                BatchId.of(1L),
                DeviceId.of("DEVICE1"),
                batchStartTime.toLocalDateTime(),
                batchStartTime.toLocalDateTime().plusMinutes(5)
        );

        BatchItem batchItem = BatchItem.builder()
                .unitNumber(new UnitNumber(unitNumber))
                .productCode(productCode)
                .productFamily("RED_BLOOD_CELLS")
                .lotNumber("LOT123")
                .irradiated(false)
                .isTimingRuleValidated(false)
                .build();

        Configuration config = Configuration.builder()
                .key(new ConfigurationKey("OUT_OF_STORAGE_RED_BLOOD_CELLS"))
                .value("30")
                .build();

        when(batchRepository.findLatestBatchWithItemByUnitProductAndDevice(unitNumber, productCode, deviceUse))
                .thenReturn(Mono.just(batch));
        when(batchRepository.findBatchItem(BatchId.of(1L), unitNumber, productCode))
                .thenReturn(Mono.just(batchItem));
        when(configurationService.readConfiguration(List.of("OUT_OF_STORAGE_RED_BLOOD_CELLS")))
                .thenReturn(Flux.just(config));

        // When
        Mono<ProcessingResult> result = outOfStorageValidationService.processProductStoredEvent(unitNumber, productCode, deviceUse, storageTime);

        // Then
        StepVerifier.create(result)
                .expectNext(new ProcessingResult(true, false, false, true))
                .verifyComplete();
    }

    @Test
    void processProductStoredEvent_WhenExceedsTimeLimit_ShouldReturnQuarantine() {
        // Given
        String unitNumber = "W036825008001";
        String productCode = "E003300";
        String deviceUse = "Collection";
        ZonedDateTime storageTime = ZonedDateTime.now();
        ZonedDateTime batchStartTime = storageTime.minusMinutes(35);

        Batch batch = new Batch(
                BatchId.of(1L),
                DeviceId.of("DEVICE1"),
                batchStartTime.toLocalDateTime(),
                batchStartTime.toLocalDateTime().plusMinutes(5)
        );

        BatchItem batchItem = BatchItem.builder()
                .unitNumber(new UnitNumber(unitNumber))
                .productCode(productCode)
                .productFamily("RED_BLOOD_CELLS")
                .lotNumber("LOT123")
                .irradiated(false)
                .isTimingRuleValidated(false)
                .build();

        Configuration config = Configuration.builder()
                .key(new ConfigurationKey("OUT_OF_STORAGE_RED_BLOOD_CELLS"))
                .value("30")
                .build();

        when(batchRepository.findLatestBatchWithItemByUnitProductAndDevice(unitNumber, productCode, deviceUse))
                .thenReturn(Mono.just(batch));
        when(batchRepository.findBatchItem(BatchId.of(1L), unitNumber, productCode))
                .thenReturn(Mono.just(batchItem));
        when(configurationService.readConfiguration(List.of("OUT_OF_STORAGE_RED_BLOOD_CELLS")))
                .thenReturn(Flux.just(config));

        // When
        Mono<ProcessingResult> result = outOfStorageValidationService.processProductStoredEvent(unitNumber, productCode, deviceUse, storageTime);

        // Then
        StepVerifier.create(result)
                .expectNext(new ProcessingResult(true, false, true, true))
                .verifyComplete();
    }

    @Test
    void processProductStoredEvent_WhenBatchNotClosed_ShouldReturnBatchNotClosed() {
        // Given
        String unitNumber = "W036825008001";
        String productCode = "E003300";
        String deviceUse = "Collection";
        ZonedDateTime storageTime = ZonedDateTime.now();
        ZonedDateTime batchStartTime = storageTime.minusMinutes(20);

        Batch batch = new Batch(
                BatchId.of(1L),
                DeviceId.of("DEVICE1"),
                batchStartTime.toLocalDateTime(),
                null // No end time
        );

        when(batchRepository.findLatestBatchWithItemByUnitProductAndDevice(unitNumber, productCode, deviceUse))
                .thenReturn(Mono.just(batch));

        // When
        Mono<ProcessingResult> result = outOfStorageValidationService.processProductStoredEvent(unitNumber, productCode, deviceUse, storageTime);

        // Then
        StepVerifier.create(result)
                .expectNext(new ProcessingResult(false, false, false, false))
                .verifyComplete();
    }

    @Test
    void processProductStoredEvent_WhenEventAlreadyProcessed_ShouldReturnAlreadyProcessed() {
        // Given
        String unitNumber = "W036825008001";
        String productCode = "E003300";
        String deviceUse = "Collection";
        ZonedDateTime storageTime = ZonedDateTime.now();
        ZonedDateTime batchStartTime = storageTime.minusMinutes(20);

        Batch batch = new Batch(
                BatchId.of(1L),
                DeviceId.of("DEVICE1"),
                batchStartTime.toLocalDateTime(),
                batchStartTime.toLocalDateTime().plusMinutes(5)
        );

        BatchItem batchItem = BatchItem.builder()
                .unitNumber(new UnitNumber(unitNumber))
                .productCode(productCode)
                .productFamily("RED_BLOOD_CELLS")
                .lotNumber("LOT123")
                .irradiated(false)
                .isTimingRuleValidated(true)
                .build();

        when(batchRepository.findLatestBatchWithItemByUnitProductAndDevice(unitNumber, productCode, deviceUse))
                .thenReturn(Mono.just(batch));
        when(batchRepository.findBatchItem(BatchId.of(1L), unitNumber, productCode))
                .thenReturn(Mono.just(batchItem));

        // When
        Mono<ProcessingResult> result = outOfStorageValidationService.processProductStoredEvent(unitNumber, productCode, deviceUse, storageTime);

        // Then
        StepVerifier.create(result)
                .expectNext(new ProcessingResult(true, true, false, false))
                .verifyComplete();
    }

    @Test
    void markEventAsProcessed_ShouldCallRepository() {
        // Given
        String unitNumber = "W036825008001";
        String productCode = "E003300";
        String deviceUse = "Collection";

        when(batchRepository.markBatchItemAsTimingRuleValidated(unitNumber, productCode))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = outOfStorageValidationService.markEventAsProcessed(unitNumber, productCode, deviceUse);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

}