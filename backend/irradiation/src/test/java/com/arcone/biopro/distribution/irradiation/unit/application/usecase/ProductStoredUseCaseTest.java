package com.arcone.biopro.distribution.irradiation.unit.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.usecase.ProductStoredUseCase;
import com.arcone.biopro.distribution.irradiation.infrastructure.event.ProductQuarantinedApplicationEvent;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.OutOfStorageValidationService;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.OutOfStorageValidationService.ProcessingResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductStoredUseCaseTest {

    @Mock
    private OutOfStorageValidationService outOfStorageValidationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductStoredUseCase productStoredUseCase;

    @Test
    void execute_WhenOutOfStorageTimeExceeded_ShouldPublishQuarantineEvent() {
        // Given
        String unitNumber = "W036825008001";
        String productCode = "E003300";
        ZonedDateTime storageTime = ZonedDateTime.now();
        String performedBy = "test-user";

        ProductStoredUseCase.Input input = ProductStoredUseCase.Input.builder()
                .unitNumber(unitNumber)
                .productCode(productCode)
                .deviceStored("Amicus")
                .deviceUse("Collection")
                .storageLocation("1FS")
                .location("1FS")
                .locationType("FREEZER")
                .storageTime(storageTime)
                .performedBy(performedBy)
                .build();

        when(outOfStorageValidationService.processProductStoredEvent(unitNumber, productCode, "Collection", storageTime))
                .thenReturn(Mono.just(new ProcessingResult(true, false, true, true)));
        when(outOfStorageValidationService.markEventAsProcessed(unitNumber, productCode, "Collection"))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = productStoredUseCase.execute(input);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<ProductQuarantinedApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ProductQuarantinedApplicationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ProductQuarantinedApplicationEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getQuarantineProduct().products()).hasSize(1);
        assertThat(capturedEvent.getQuarantineProduct().products().get(0).unitNumber()).isEqualTo(unitNumber);
        assertThat(capturedEvent.getQuarantineProduct().products().get(0).productCode()).isEqualTo(productCode);
        assertThat(capturedEvent.getQuarantineProduct().triggeredBy()).isEqualTo("IRRADIATION_SYSTEM");
        assertThat(capturedEvent.getQuarantineProduct().reasonKey()).isEqualTo("OUT_OF_STORAGE_TIME_EXCEEDED");
        assertThat(capturedEvent.getQuarantineProduct().performedBy()).isEqualTo(performedBy);
    }

    @Test
    void execute_WhenOutOfStorageTimeNotExceeded_ShouldNotPublishQuarantineEvent() {
        // Given
        String unitNumber = "W036825008001";
        String productCode = "E003300";
        ZonedDateTime storageTime = ZonedDateTime.now();

        ProductStoredUseCase.Input input = ProductStoredUseCase.Input.builder()
                .unitNumber(unitNumber)
                .productCode(productCode)
                .deviceStored("Amicus")
                .deviceUse("Collection")
                .storageLocation("1FS")
                .location("1FS")
                .locationType("FREEZER")
                .storageTime(storageTime)
                .performedBy("test-user")
                .build();

        when(outOfStorageValidationService.processProductStoredEvent(unitNumber, productCode, "Collection", storageTime))
                .thenReturn(Mono.just(new ProcessingResult(true, false, false, true)));
        when(outOfStorageValidationService.markEventAsProcessed(unitNumber, productCode, "Collection"))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = productStoredUseCase.execute(input);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void execute_WhenBatchIsNotClosed_ShouldNotProcessEvent() {
        // Given
        String unitNumber = "W036825008001";
        String productCode = "E003300";
        ZonedDateTime storageTime = ZonedDateTime.now();

        ProductStoredUseCase.Input input = ProductStoredUseCase.Input.builder()
                .unitNumber(unitNumber)
                .productCode(productCode)
                .deviceStored("Amicus")
                .deviceUse("Collection")
                .storageLocation("1FS")
                .location("1FS")
                .locationType("FREEZER")
                .storageTime(storageTime)
                .performedBy("test-user")
                .build();

        when(outOfStorageValidationService.processProductStoredEvent(unitNumber, productCode, "Collection", storageTime))
                .thenReturn(Mono.just(new ProcessingResult(false, false, false, false)));

        // When
        Mono<Void> result = productStoredUseCase.execute(input);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(eventPublisher, never()).publishEvent(any());
        verify(outOfStorageValidationService, never()).markEventAsProcessed(any(), any(), any());
    }

    @Test
    void execute_WhenProcessingServiceFails_ShouldPropagateError() {
        // Given
        String unitNumber = "W036825008001";
        String productCode = "E003300";
        ZonedDateTime storageTime = ZonedDateTime.now();

        ProductStoredUseCase.Input input = ProductStoredUseCase.Input.builder()
                .unitNumber(unitNumber)
                .productCode(productCode)
                .deviceStored("Amicus")
                .deviceUse("Collection")
                .storageLocation("1FS")
                .location("1FS")
                .locationType("FREEZER")
                .storageTime(storageTime)
                .performedBy("test-user")
                .build();

        when(outOfStorageValidationService.processProductStoredEvent(unitNumber, productCode, "Collection", storageTime))
                .thenReturn(Mono.error(new RuntimeException("Processing failed")));

        // When
        Mono<Void> result = productStoredUseCase.execute(input);

        // Then
        StepVerifier.create(result)
                .verifyError(RuntimeException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void execute_WhenEventAlreadyProcessed_ShouldNotProcessEvent() {
        // Given
        String unitNumber = "W036825008001";
        String productCode = "E003300";
        ZonedDateTime storageTime = ZonedDateTime.now();

        ProductStoredUseCase.Input input = ProductStoredUseCase.Input.builder()
                .unitNumber(unitNumber)
                .productCode(productCode)
                .deviceStored("Amicus")
                .deviceUse("Collection")
                .storageLocation("1FS")
                .location("1FS")
                .locationType("FREEZER")
                .storageTime(storageTime)
                .performedBy("test-user")
                .build();

        when(outOfStorageValidationService.processProductStoredEvent(unitNumber, productCode, "Collection", storageTime))
                .thenReturn(Mono.just(new ProcessingResult(true, true, false, false)));

        // When
        Mono<Void> result = productStoredUseCase.execute(input);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(eventPublisher, never()).publishEvent(any());
        verify(outOfStorageValidationService, never()).markEventAsProcessed(any(), any(), any());
    }
}
