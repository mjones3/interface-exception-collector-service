package com.arcone.biopro.distribution.receiving.unit.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.CompleteImportCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ImportOutputMapper;
import com.arcone.biopro.distribution.receiving.application.usecase.CompleteImportUseCase;
import com.arcone.biopro.distribution.receiving.domain.event.ImportCompletedDomainEvent;
import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteImportUseCaseTest {

    @Mock
    private ImportRepository importRepository;

    @Mock
    private ImportOutputMapper importOutputMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CompleteImportUseCase completeImportUseCase;

    @Test
    void shouldCompleteImportSuccessfully() {
        // Arrange
        Long importId = 123L;
        String employeeId = "emp456";
        CompleteImportCommandInput commandInput = new CompleteImportCommandInput(importId, employeeId);

        Import pendingImport = mock(Import.class);
        Import completedImport = mock(Import.class);
        ImportOutput importOutput = mock(ImportOutput.class);

        when(importRepository.findOneById(importId)).thenReturn(Mono.just(pendingImport));
        when(pendingImport.completeImport(employeeId)).thenReturn(completedImport);
        when(importRepository.update(completedImport)).thenReturn(Mono.just(completedImport));
        when(importOutputMapper.toOutput(completedImport)).thenReturn(importOutput);

        // Act
        Mono<UseCaseOutput<ImportOutput>> result = completeImportUseCase.completeImport(commandInput);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertEquals(importOutput, output.data());
                assertEquals(1, output.notifications().size());

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals(UseCaseMessageType.IMPORT_COMPLETED_SUCCESS.getMessage(),
                    notification.useCaseMessage().message());
                assertEquals(UseCaseMessageType.IMPORT_COMPLETED_SUCCESS.getCode(),
                    notification.useCaseMessage().code());
                assertEquals(UseCaseMessageType.IMPORT_COMPLETED_SUCCESS.getType(),
                    notification.useCaseMessage().type());
                assertEquals("imports/imports-enter-shipment-information",
                    output._links().get("next"));

            })
            .verifyComplete();

        verify(applicationEventPublisher).publishEvent(Mockito.any(ImportCompletedDomainEvent.class));
    }

    @Test
    void shouldHandleImportNotFound() {
        // Arrange
        Long importId = 1L;
        String employeeId = "emp456";
        CompleteImportCommandInput commandInput = new CompleteImportCommandInput(importId, employeeId);

        when(importRepository.findOneById(importId))
            .thenReturn(Mono.empty());

        // Act
        Mono<UseCaseOutput<ImportOutput>> result = completeImportUseCase.completeImport(commandInput);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                assertEquals(1, output.notifications().size());

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals("Domain not found for key 1", notification.useCaseMessage().message());
                assertEquals(12, notification.useCaseMessage().code());
                assertEquals(UseCaseNotificationType.WARN, notification.useCaseMessage().type());

                verify(applicationEventPublisher, never()).publishEvent(any(ImportCompletedDomainEvent.class));
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleUpdateError() {
        // Arrange
        Long importId = 123L;
        String employeeId = "emp456";
        CompleteImportCommandInput commandInput = new CompleteImportCommandInput(importId, employeeId);

        Import pendingImport = mock(Import.class);
        Import completedImport = mock(Import.class);
        RuntimeException updateError = new RuntimeException("Update failed");

        when(importRepository.findOneById(importId)).thenReturn(Mono.just(pendingImport));
        when(pendingImport.completeImport(employeeId)).thenReturn(completedImport);
        when(importRepository.update(completedImport)).thenReturn(Mono.error(updateError));

        // Act
        Mono<UseCaseOutput<ImportOutput>> result = completeImportUseCase.completeImport(commandInput);

        // Assert
        StepVerifier.create(result)
            .assertNext(output -> {
                assertNotNull(output);
                assertNull(output.data());
                assertEquals(1, output.notifications().size());

                UseCaseNotificationOutput notification = output.notifications().get(0);
                assertEquals("Update failed", notification.useCaseMessage().message());
                assertEquals(12, notification.useCaseMessage().code());
                assertEquals(UseCaseNotificationType.WARN, notification.useCaseMessage().type());

                verify(applicationEventPublisher, never()).publishEvent(any(ImportCompletedDomainEvent.class));
            })
            .verifyComplete();
    }
}

