package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.AcknowledgeRequest;
import com.arcone.biopro.exception.collector.api.dto.AcknowledgeResponse;
import com.arcone.biopro.exception.collector.api.dto.ResolveRequest;
import com.arcone.biopro.exception.collector.api.dto.ResolveResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import com.arcone.biopro.exception.collector.infrastructure.kafka.publisher.ExceptionEventPublisher;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ExceptionManagementService.
 * Tests acknowledgment and resolution functionality as per requirements US-013
 * and US-014.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExceptionManagementService Tests")
class ExceptionManagementServiceTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private ExceptionEventPublisher eventPublisher;

    @InjectMocks
    private ExceptionManagementService managementService;

    private InterfaceException testException;
    private final String transactionId = "test-transaction-123";
    private final String acknowledgedBy = "john.doe@company.com";
    private final String resolvedBy = "jane.smith@company.com";

    @BeforeEach
    void setUp() {
        testException = InterfaceException.builder()
                .id(1L)
                .transactionId(transactionId)
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception reason")
                .operation("CREATE_ORDER")
                .externalId("ORDER-123")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.BUSINESS_RULE)
                .retryable(true)
                .customerId("CUST001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .build();
    }

@Test
    @DisplayName("Should acknowledge exception successfully")
    void shouldAcknowledgeExceptionSuccessfully() {
        // Given
        AcknowledgeRequest request = AcknowledgeRequest.builder()
                .acknowledgedBy(acknowledgedBy)
                .notes("Reviewed and assigned to development team")
                .build();

        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(testException));
        when(exceptionRepository.save(any(InterfaceException.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AcknowledgeResponse response = managementService.acknowledgeException(transactionId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ACKNOWLEDGED");
        assertThat(response.getAcknowledgedBy()).isEqualTo(acknowledgedBy);
        assertThat(response.getNotes()).isEqualTo("Reviewed and assigned to development team");
        assertThat(response.getTransactionId()).isEqualTo(transactionId);
        assertThat(response.getAcknowledgedAt()).isNotNull();

        // Verify exception was updated
        ArgumentCaptor<InterfaceException> exceptionCaptor = ArgumentCaptor.forClass(InterfaceException.class);
        verify(exceptionRepository).save(exceptionCaptor.capture());
        
        InterfaceException savedException = exceptionCaptor.getValue();
        assertThat(savedException.getStatus()).isEqualTo(ExceptionStatus.ACKNOWLEDGED);
        assertThat(savedException.getAcknowledgedBy()).isEqualTo(acknowledgedBy);
        assertThat(savedException.getAcknowledgmentNotes()).isEqualTo("Reviewed and assigned to development team");
        assertThat(savedException.getAcknowledgedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when acknowledging non-existent exception")
    void shouldThrowExceptionWhenAcknowledgingNonExistentException() {
        // Given
        AcknowledgeRequest request = AcknowledgeRequest.builder()
                .acknowledgedBy(acknowledgedBy)
                .notes("Test notes")
                .build();

        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> managementService.acknowledgeException(transactionId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exception not found with transaction ID: " + transactionId);

        verify(exceptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should resolve exception successfully")
    void shouldResolveExceptionSuccessfully() {
        // Given
        ResolveRequest request = ResolveRequest.builder()
                .resolvedBy(resolvedBy)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Fixed data validation issue in source system")
                .build();

        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(testException));
        when(exceptionRepository.save(any(InterfaceException.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(eventPublisher.publishExceptionResolved(any(InterfaceException.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // When
        ResolveResponse response = managementService.resolveException(transactionId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("RESOLVED");
        assertThat(response.getResolvedBy()).isEqualTo(resolvedBy);
        assertThat(response.getResolutionMethod()).isEqualTo(ResolutionMethod.MANUAL_RESOLUTION);
        assertThat(response.getResolutionNotes()).isEqualTo("Fixed data validation issue in source system");
        assertThat(response.getTransactionId()).isEqualTo(transactionId);
        assertThat(response.getResolvedAt()).isNotNull();
        assertThat(response.getTotalRetryAttempts()).isEqualTo(0);

        // Verify exception was updated
        ArgumentCaptor<InterfaceException> exceptionCaptor = ArgumentCaptor.forClass(InterfaceException.class);
        verify(exceptionRepository).save(exceptionCaptor.capture());
        
        InterfaceException savedException = exceptionCaptor.getValue();
        assertThat(savedException.getStatus()).isEqualTo(ExceptionStatus.RESOLVED);
        assertThat(savedException.getResolvedBy()).isEqualTo(resolvedBy);
        assertThat(savedException.getResolutionMethod()).isEqualTo(ResolutionMethod.MANUAL_RESOLUTION);
        assertThat(savedException.getResolutionNotes()).isEqualTo("Fixed data validation issue in source system");
        assertThat(savedException.getResolvedAt()).isNotNull();

        // Verify event was published
        verify(eventPublisher).publishExceptionResolved(savedException);
    }

    @Test
    @DisplayName("Should throw exception when resolving non-existent exception")
    void shouldThrowExceptionWhenResolvingNonExistentException() {
        // Given
        ResolveRequest request = ResolveRequest.builder()
                .resolvedBy(resolvedBy)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test notes")
                .build();

        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> managementService.resolveException(transactionId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exception not found with transaction ID: " + transactionId);

        verify(exceptionRepository, never()).save(any());
        verify(eventPublisher, never()).publishExceptionResolved(any());
    }

    @Test
    @DisplayName("Should continue resolution even if event publishing fails")
    void shouldContinueResolutionEvenIfEventPublishingFails() {
        // Given
        ResolveRequest request = ResolveRequest.builder()
                .resolvedBy(resolvedBy)
                .resolutionMethod(ResolutionMethod.AUTOMATED)
                .resolutionNotes("Automated resolution")
                .build();

        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(testException));
        when(exceptionRepository.save(any(InterfaceException.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(eventPublisher.publishExceptionResolved(any(InterfaceException.class)))
                .thenThrow(new RuntimeException("Kafka publishing failed"));

        // When
        ResolveResponse response = managementService.resolveException(transactionId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("RESOLVED");
        assertThat(response.getResolvedBy()).isEqualTo(resolvedBy);
        assertThat(response.getResolutionMethod()).isEqualTo(ResolutionMethod.AUTOMATED);

        // Verify exception was still saved despite event publishing failure
        verify(exceptionRepository).save(any(InterfaceException.class));
        verify(eventPublisher).publishExceptionResolved(any(InterfaceException.class));
    }

    @Test
    @DisplayName("Should return true when exception can be acknowledged")
    void shouldReturnTrueWhenExceptionCanBeAcknowledged() {
        // Given
        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(testException));

        // When
        boolean canAcknowledge = managementService.canAcknowledge(transactionId);

        // Then
        assertThat(canAcknowledge).isTrue();
    }

    @Test
    @DisplayName("Should return false when exception cannot be acknowledged - already resolved")
    void shouldReturnFalseWhenExceptionCannotBeAcknowledgedAlreadyResolved() {
        // Given
        testException.setStatus(ExceptionStatus.RESOLVED);
        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(testException));

        // When
        boolean canAcknowledge = managementService.canAcknowledge(transactionId);

        // Then
        assertThat(canAcknowledge).isFalse();
    }

    @Test
    @DisplayName("Should return false when exception cannot be acknowledged - already closed")
    void shouldReturnFalseWhenExceptionCannotBeAcknowledgedAlreadyClosed() {
        // Given
        testException.setStatus(ExceptionStatus.CLOSED);
        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(testException));

        // When
        boolean canAcknowledge = managementService.canAcknowledge(transactionId);

        // Then
        assertThat(canAcknowledge).isFalse();
    }

    @Test
    @DisplayName("Should return false when exception cannot be acknowledged - not found")
    void shouldReturnFalseWhenExceptionCannotBeAcknowledgedNotFound() {
        // Given
        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        // When
        boolean canAcknowledge = managementService.canAcknowledge(transactionId);

        // Then
        assertThat(canAcknowledge).isFalse();
    }

    @Test
    @DisplayName("Should return true when exception can be resolved")
    void shouldReturnTrueWhenExceptionCanBeResolved() {
        // Given
        testException.setStatus(ExceptionStatus.ACKNOWLEDGED);
        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(testException));

        // When
        boolean canResolve = managementService.canResolve(transactionId);

        // Then
        assertThat(canResolve).isTrue();
    }

    @Test
    @DisplayName("Should return false when exception cannot be resolved - already resolved")
    void shouldReturnFalseWhenExceptionCannotBeResolvedAlreadyResolved() {
        // Given
        testException.setStatus(ExceptionStatus.RESOLVED);
        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(testException));

        // When
        boolean canResolve = managementService.canResolve(transactionId);

        // Then
        assertThat(canResolve).isFalse();
    }

    @Test
    @DisplayName("Should return exception status when found")
    void shouldReturnExceptionStatusWhenFound() {
        // Given
        testException.setStatus(ExceptionStatus.ACKNOWLEDGED);
        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(testException));

        // When
        Optional<ExceptionStatus> status = managementService.getExceptionStatus(transactionId);

        // Then
        assertThat(status).isPresent();
        assertThat(status.get()).isEqualTo(ExceptionStatus.ACKNOWLEDGED);
    }

    @Test
    @DisplayName("Should return empty when exception not found")
    void shouldReturnEmptyWhenExceptionNotFound() {
        // Given
        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        // When
        Optional<ExceptionStatus> status = managementService.getExceptionStatus(transactionId);

        // Then
        assertThat(status).isEmpty();
    }
}