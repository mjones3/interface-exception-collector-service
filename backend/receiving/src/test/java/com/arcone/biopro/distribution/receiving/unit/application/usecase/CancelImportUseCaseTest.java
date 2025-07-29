package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.CancelImportCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.usecase.CancelImportUseCase;
import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelImportUseCaseTest {

    @Mock
    private ImportRepository importRepository;

    private CancelImportUseCase cancelImportUseCase;

    @BeforeEach
    void setUp() {
        cancelImportUseCase = new CancelImportUseCase(importRepository);
    }

    @Test
    void shouldSuccessfullyCancelImport() {
        // Given
        Long importId = 123L;
        CancelImportCommandInput input = new CancelImportCommandInput(importId,"EmployeeId");
        Import pendingImport = Mockito.mock(Import.class);
        when(pendingImport.getId()).thenReturn(importId);
        when(pendingImport.validateCancel()).thenReturn(pendingImport);

        when(importRepository.findOneById(importId)).thenReturn(Mono.just(pendingImport));
        when(importRepository.deleteOneById(importId)).thenReturn(Mono.empty());

        // When
        Mono<UseCaseOutput<Void>> result = cancelImportUseCase.cancelImport(input);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assertThat(output.notifications()).hasSize(1);
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertThat(notification.useCaseMessage().message())
                    .isEqualTo(UseCaseMessageType.IMPORT_CANCELED_SUCCESS.getMessage());
                assertThat(notification.useCaseMessage().code())
                    .isEqualTo(UseCaseMessageType.IMPORT_CANCELED_SUCCESS.getCode());
                assertThat(notification.useCaseMessage().type())
                    .isEqualTo(UseCaseMessageType.IMPORT_CANCELED_SUCCESS.getType());
                assertThat(output._links())
                    .containsEntry("next", "imports/imports-enter-shipment-information");
            })
            .verifyComplete();

        verify(importRepository).findOneById(importId);
        verify(importRepository).deleteOneById(importId);
    }

    @Test
    void shouldHandleImportNotFound() {
        // Given
        Long importId = 123L;
        CancelImportCommandInput input = new CancelImportCommandInput(importId,"EmployeeId");

        when(importRepository.findOneById(importId))
            .thenReturn(Mono.empty());

        // When
        Mono<UseCaseOutput<Void>> result = cancelImportUseCase.cancelImport(input);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assertThat(output.notifications()).hasSize(1);
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertThat(notification.useCaseMessage().code())
                    .isEqualTo(13);
                assertThat(notification.useCaseMessage().type())
                    .isEqualTo(UseCaseNotificationType.WARN);
                assertThat(output._links()).isNull();
            })
            .verifyComplete();

        verify(importRepository).findOneById(importId);
        verify(importRepository, Mockito.never()).deleteOneById(any());
    }

    @Test
    void shouldHandleDeleteError() {
        // Given
        Long importId = 123L;
        CancelImportCommandInput input = new CancelImportCommandInput(importId,"EmployeeId");
        Import pendingImport = Mockito.mock(Import.class);
        when(pendingImport.getId()).thenReturn(importId);
        when(pendingImport.validateCancel()).thenReturn(pendingImport);

        when(importRepository.findOneById(importId))
            .thenReturn(Mono.just(pendingImport));
        when(importRepository.deleteOneById(importId))
            .thenReturn(Mono.error(new RuntimeException("Delete failed")));

        // When
        Mono<UseCaseOutput<Void>> result = cancelImportUseCase.cancelImport(input);

        // Then
        StepVerifier.create(result)
            .assertNext(output -> {
                assertThat(output.notifications()).hasSize(1);
                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertThat(notification.useCaseMessage().message())
                    .isEqualTo("Delete failed");
                assertThat(notification.useCaseMessage().code())
                    .isEqualTo(13);
                assertThat(notification.useCaseMessage().type())
                    .isEqualTo(UseCaseNotificationType.WARN);
                assertThat(output._links()).isNull();
            })
            .verifyComplete();

        verify(importRepository).findOneById(importId);
        verify(importRepository).deleteOneById(importId);
    }
}
