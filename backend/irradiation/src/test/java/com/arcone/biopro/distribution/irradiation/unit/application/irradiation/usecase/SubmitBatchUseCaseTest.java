package com.arcone.biopro.distribution.irradiation.unit.application.irradiation.usecase;

import com.arcone.biopro.distribution.irradiation.application.irradiation.command.SubmitBatchCommand;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchItemDTO;
import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.application.irradiation.mapper.BatchMapper;
import com.arcone.biopro.distribution.irradiation.application.irradiation.usecase.SubmitBatchUseCase;
import com.arcone.biopro.distribution.irradiation.domain.exception.BatchSubmissionException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.BatchRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitBatchUseCaseTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private BatchMapper batchMapper;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private SubmitBatchUseCase submitBatchUseCase;

    @Test
    void execute_ShouldReturnSuccessResult_WhenBatchSubmittedSuccessfully() {
        // Given
        SubmitBatchCommand command = SubmitBatchCommand.builder()
                .deviceId("1L")
                .startTime(LocalDateTime.now())
                .batchItems(List.of(
                    BatchItemDTO.builder().unitNumber("W777725001001").productCode("PROD001").lotNumber("LOT001").build(),
                    BatchItemDTO.builder().unitNumber("W777725001002").productCode("PROD002").lotNumber("LOT002").build()
                ))
                .build();

        Inventory inventory1 = Inventory.builder()
                .unitNumber(new UnitNumber("W777725001001"))
                .productCode("PROD001")
                .location(new Location("LOC1"))
                .status("AVAILABLE")
                .productFamily("PLASMA")
                .expirationDate(LocalDateTime.now().plusDays(30))
                .build();

        Inventory inventory2 = Inventory.builder()
                .unitNumber(new UnitNumber("W777725001002"))
                .productCode("PROD002")
                .location(new Location("LOC1"))
                .status("AVAILABLE")
                .productFamily("RBC")
                .expirationDate(LocalDateTime.now().plusDays(45))
                .build();

        Batch batch = new Batch(BatchId.of(100L), DeviceId.of(1L), LocalDateTime.now(), null);
        BatchSubmissionResultDTO expectedResult = BatchSubmissionResultDTO.builder()
                .batchId(100L)
                .message("Batch submitted successfully for irradiation")
                .success(true)
                .build();

        when(inventoryClient.getInventoryByUnitNumber(new UnitNumber("W777725001001")))
                .thenReturn(Flux.just(inventory1));
        when(inventoryClient.getInventoryByUnitNumber(new UnitNumber("W777725001002")))
                .thenReturn(Flux.just(inventory2));
        when(batchRepository.submitBatch(any(DeviceId.class), any(), any())).thenReturn(Mono.just(batch));
        when(batchMapper.toSubmissionResult(batch)).thenReturn(expectedResult);

        // When & Then
        StepVerifier.create(submitBatchUseCase.execute(command))
                .expectNext(expectedResult)
                .verifyComplete();
    }

    @Test
    void execute_ShouldReturnError_WhenRepositoryFails() {
        // Given
        SubmitBatchCommand command = SubmitBatchCommand.builder()
                .deviceId("1L")
                .startTime(LocalDateTime.now())
                .batchItems(List.of(
                    BatchItemDTO.builder().unitNumber("W777725001001").productCode("PROD001").lotNumber("LOT001").build()
                ))
                .build();

        Inventory inventory = Inventory.builder()
                .unitNumber(new UnitNumber("W777725001001"))
                .productCode("PROD001")
                .location(new Location("LOC1"))
                .status("AVAILABLE")
                .productFamily("PLASMA")
                .expirationDate(LocalDateTime.now().plusDays(30))
                .build();

        when(inventoryClient.getInventoryByUnitNumber(new UnitNumber("W777725001001")))
                .thenReturn(Flux.just(inventory));
        when(batchRepository.submitBatch(any(DeviceId.class), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // When & Then
        StepVerifier.create(submitBatchUseCase.execute(command))
                .expectError(BatchSubmissionException.class)
                .verify();
    }
}
