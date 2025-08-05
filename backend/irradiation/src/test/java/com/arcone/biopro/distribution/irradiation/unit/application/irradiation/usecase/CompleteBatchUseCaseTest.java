package com.arcone.biopro.distribution.irradiation.unit.application.irradiation.usecase;

import com.arcone.biopro.distribution.irradiation.application.irradiation.command.CompleteBatchCommand;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchItemCompletionDTO;
import com.arcone.biopro.distribution.irradiation.application.irradiation.usecase.CompleteBatchUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate.IrradiationAggregate;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.BatchCompletionService;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompleteBatchUseCaseTest {

    @Mock
    private BatchCompletionService batchCompletionService;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private IrradiationAggregate mockAggregate;
    @Mock
    private Batch mockBatch;

    private CompleteBatchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CompleteBatchUseCase(batchCompletionService, batchRepository);
    }

    @Test
    void shouldCompleteBatchWithIrradiatedAndNonIrradiatedItems() {
        // Given
        String deviceId = "AUTO-DEVICE123";
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
                .deviceId(deviceId)
                .endTime(endTime)
                .batchItems(List.of(irradiatedItem, nonIrradiatedItem))
                .build();

        // Setup mocks
        when(mockBatch.getId()).thenReturn(BatchId.of(123L));
        when(batchRepository.findActiveBatchByDeviceId(DeviceId.of(deviceId)))
                .thenReturn(Mono.just(mockBatch));
        when(batchCompletionService.prepareBatchCompletion(eq(BatchId.of(123L)), any(), eq(endTime)))
                .thenReturn(Mono.just(mockAggregate));
        when(batchCompletionService.completeBatch(mockAggregate))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(command))
                .assertNext(result -> {
                    assertThat(result.batchId()).isEqualTo(123L);
                    assertThat(result.message()).isEqualTo("Batch completed successfully");
                    assertThat(result.success()).isTrue();
                })
                .verifyComplete();

        // Verify service calls
        verify(batchRepository).findActiveBatchByDeviceId(DeviceId.of(deviceId));
        verify(batchCompletionService).prepareBatchCompletion(eq(BatchId.of(123L)), any(), eq(endTime));
        verify(batchCompletionService).completeBatch(mockAggregate);
    }

    @Test
    void shouldHandleOnlyIrradiatedItems() {
        // Given
        String deviceId = "AUTO-DEVICE123";
        var irradiatedItem = BatchItemCompletionDTO.builder()
                .unitNumber("W123456789001")
                .productCode("E003300")
                .isIrradiated(true)
                .build();

        var command = CompleteBatchCommand.builder()
                .deviceId(deviceId)
                .endTime(LocalDateTime.now())
                .batchItems(List.of(irradiatedItem))
                .build();

        // Setup mocks
        when(mockBatch.getId()).thenReturn(BatchId.of(123L));
        when(batchRepository.findActiveBatchByDeviceId(DeviceId.of(deviceId)))
                .thenReturn(Mono.just(mockBatch));
        when(batchCompletionService.prepareBatchCompletion(any(), any(), any()))
                .thenReturn(Mono.just(mockAggregate));
        when(batchCompletionService.completeBatch(mockAggregate))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(command))
                .assertNext(result -> assertThat(result.success()).isTrue())
                .verifyComplete();

        verify(batchRepository).findActiveBatchByDeviceId(DeviceId.of(deviceId));
        verify(batchCompletionService).prepareBatchCompletion(any(), any(), any());
        verify(batchCompletionService).completeBatch(mockAggregate);
    }

    @Test
    void shouldHandleOnlyNonIrradiatedItems() {
        // Given
        String deviceId = "AUTO-DEVICE123";
        var nonIrradiatedItem = BatchItemCompletionDTO.builder()
                .unitNumber("W123456789002")
                .productCode("E003301")
                .isIrradiated(false)
                .build();

        var command = CompleteBatchCommand.builder()
                .deviceId(deviceId)
                .endTime(LocalDateTime.now())
                .batchItems(List.of(nonIrradiatedItem))
                .build();

        // Setup mocks
        when(mockBatch.getId()).thenReturn(BatchId.of(123L));
        when(batchRepository.findActiveBatchByDeviceId(DeviceId.of(deviceId)))
                .thenReturn(Mono.just(mockBatch));
        when(batchCompletionService.prepareBatchCompletion(any(), any(), any()))
                .thenReturn(Mono.just(mockAggregate));
        when(batchCompletionService.completeBatch(mockAggregate))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(command))
                .assertNext(result -> assertThat(result.success()).isTrue())
                .verifyComplete();

        verify(batchRepository).findActiveBatchByDeviceId(DeviceId.of(deviceId));
        verify(batchCompletionService).prepareBatchCompletion(any(), any(), any());
        verify(batchCompletionService).completeBatch(mockAggregate);
    }

    @Test
    void shouldHandleEmptyBatchItems() {
        // Given
        String deviceId = "AUTO-DEVICE123";
        var command = CompleteBatchCommand.builder()
                .deviceId(deviceId)
                .endTime(LocalDateTime.now())
                .batchItems(Collections.emptyList())
                .build();

        // Setup mocks
        when(mockBatch.getId()).thenReturn(BatchId.of(123L));
        when(batchRepository.findActiveBatchByDeviceId(DeviceId.of(deviceId)))
                .thenReturn(Mono.just(mockBatch));
        when(batchCompletionService.prepareBatchCompletion(any(), any(), any()))
                .thenReturn(Mono.just(mockAggregate));
        when(batchCompletionService.completeBatch(mockAggregate))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(command))
                .expectNextCount(1)
                .verifyComplete();

        verify(batchRepository).findActiveBatchByDeviceId(DeviceId.of(deviceId));
        verify(batchCompletionService).prepareBatchCompletion(any(), any(), any());
        verify(batchCompletionService).completeBatch(mockAggregate);
    }

    @Test
    void shouldHandleNoActiveBatchFound() {
        // Given
        String deviceId = "NONEXISTENT-DEVICE";
        var command = CompleteBatchCommand.builder()
                .deviceId(deviceId)
                .endTime(LocalDateTime.now())
                .batchItems(Collections.emptyList())
                .build();

        // Setup mocks
        when(batchRepository.findActiveBatchByDeviceId(DeviceId.of(deviceId)))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.execute(command))
                .expectErrorMessage("No active batch found for device: " + deviceId)
                .verify();

        verify(batchRepository).findActiveBatchByDeviceId(DeviceId.of(deviceId));
        verifyNoInteractions(batchCompletionService);
    }

    @Test
    void shouldVerifyBatchItemCompletionMapping() {
        // Given
        String deviceId = "AUTO-DEVICE123";
        var item1 = BatchItemCompletionDTO.builder()
                .unitNumber("W123456789001")
                .productCode("E003300")
                .isIrradiated(true)
                .build();

        var item2 = BatchItemCompletionDTO.builder()
                .unitNumber("W123456789002")
                .productCode("E003301")
                .isIrradiated(false)
                .build();

        var command = CompleteBatchCommand.builder()
                .deviceId(deviceId)
                .endTime(LocalDateTime.now())
                .batchItems(List.of(item1, item2))
                .build();

        // Setup mocks
        when(mockBatch.getId()).thenReturn(BatchId.of(123L));
        when(batchRepository.findActiveBatchByDeviceId(DeviceId.of(deviceId)))
                .thenReturn(Mono.just(mockBatch));
        when(batchCompletionService.prepareBatchCompletion(any(), any(), any()))
                .thenReturn(Mono.just(mockAggregate));
        when(batchCompletionService.completeBatch(mockAggregate))
                .thenReturn(Mono.empty());

        // When
        StepVerifier.create(useCase.execute(command))
                .expectNextCount(1)
                .verifyComplete();

        // Then - verify the mapping of BatchItemCompletion objects
        verify(batchRepository).findActiveBatchByDeviceId(DeviceId.of(deviceId));
        verify(batchCompletionService).prepareBatchCompletion(
                eq(BatchId.of(123L)),
                argThat(completions -> {
                    assertThat(completions).hasSize(2);
                    assertThat(completions.get(0).unitNumber()).isEqualTo("W123456789001");
                    assertThat(completions.get(0).productCode()).isEqualTo("E003300");
                    assertThat(completions.get(0).isIrradiated()).isTrue();
                    assertThat(completions.get(1).unitNumber()).isEqualTo("W123456789002");
                    assertThat(completions.get(1).productCode()).isEqualTo("E003301");
                    assertThat(completions.get(1).isIrradiated()).isFalse();
                    return true;
                }),
                any()
        );
    }
}