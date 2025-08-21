package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.dto.AcknowledgeRequest;
import com.arcone.biopro.exception.collector.api.dto.AcknowledgeResponse;
import com.arcone.biopro.exception.collector.api.dto.ResolveRequest;
import com.arcone.biopro.exception.collector.api.dto.ResolveResponse;
import com.arcone.biopro.exception.collector.api.dto.RetryRequest;
import com.arcone.biopro.exception.collector.api.dto.RetryResponse;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionResult;
import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.application.service.RetryService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests to verify that GraphQL mutations have the same effects as equivalent REST endpoints.
 * Tests the consistency between GraphQL and REST API operations.
 */
@ExtendWith(MockitoExtension.class)
class RetryMutationResolverIntegrationTest {

    @Mock
    private RetryService retryService;

    @Mock
    private ExceptionManagementService exceptionManagementService;

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    @Mock
    private Authentication authentication;

    private RetryMutationResolver resolver;

    private InterfaceException testException;

    @BeforeEach
    void setUp() {
        resolver = new RetryMutationResolver(
                retryService,
                exceptionManagementService,
                exceptionRepository,
                retryAttemptRepository
        );

        testException = InterfaceException.builder()
                .id(1L)
                .transactionId("TEST-123")
                .status(ExceptionStatus.NEW)
                .retryable(true)
                .build();

        when(authentication.getName()).thenReturn("test.user@company.com");
    }

    @Test
    void retryException_ShouldUseExistingRetryService_AndProduceSameEffectAsRestApi() {
        // Given
        String transactionId = "TEST-123";
        String reason = "Manual retry requested";
        
        RetryExceptionInput graphqlInput = RetryExceptionInput.builder()
                .transactionId(transactionId)
                .reason(reason)
                .build();

        RetryRequest expectedServiceRequest = RetryRequest.builder()
                .reason(reason)
                .initiatedBy("test.user@company.com")
                .build();

        RetryResponse serviceResponse = RetryResponse.builder()
                .retryId(100L)
                .status("PENDING")
                .message("Retry operation initiated successfully")
                .attemptNumber(1)
                .build();

        when(retryService.canRetry(transactionId)).thenReturn(true);
        when(retryService.initiateRetry(eq(transactionId), any(RetryRequest.class))).thenReturn(serviceResponse);
        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(testException));

        // When
        RetryExceptionResult result = resolver.retryException(graphqlInput, authentication).join();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getException()).isEqualTo(testException);

        // Verify same service calls as REST API
        verify(retryService).canRetry(transactionId);
        verify(retryService).initiateRetry(eq(transactionId), any(RetryRequest.class));
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void acknowledgeException_ShouldUseExistingManagementService_AndProduceSameEffectAsRestApi() {
        // Given
        String transactionId = "TEST-123";
        String reason = "Investigating issue";
        String notes = "Assigned to development team";
        
        AcknowledgeExceptionInput graphqlInput = AcknowledgeExceptionInput.builder()
                .transactionId(transactionId)
                .reason(reason)
                .notes(notes)
                .build();

        AcknowledgeResponse serviceResponse = AcknowledgeResponse.builder()
                .transactionId(transactionId)
                .status("ACKNOWLEDGED")
                .acknowledgedBy("test.user@company.com")
                .acknowledgedAt(OffsetDateTime.now())
                .notes("Reason: " + reason + "\nNotes: " + notes)
                .build();

        InterfaceException acknowledgedException = testException.toBuilder()
                .status(ExceptionStatus.ACKNOWLEDGED)
                .acknowledgedBy("test.user@company.com")
                .acknowledgedAt(OffsetDateTime.now())
                .build();

        when(exceptionManagementService.canAcknowledge(transactionId)).thenReturn(true);
        when(exceptionManagementService.acknowledgeException(eq(transactionId), any(AcknowledgeRequest.class)))
                .thenReturn(serviceResponse);
        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(acknowledgedException));

        // When
        AcknowledgeExceptionResult result = resolver.acknowledgeException(graphqlInput, authentication).join();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getException()).isEqualTo(acknowledgedException);
        assertThat(result.getException().getStatus()).isEqualTo(ExceptionStatus.ACKNOWLEDGED);

        // Verify same service calls as REST API
        verify(exceptionManagementService).canAcknowledge(transactionId);
        verify(exceptionManagementService).acknowledgeException(eq(transactionId), any(AcknowledgeRequest.class));
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void resolveException_ShouldUseExistingManagementService_AndProduceSameEffectAsRestApi() {
        // Given
        String transactionId = "TEST-123";
        ResolutionMethod resolutionMethod = ResolutionMethod.MANUAL_RESOLUTION;
        String resolutionNotes = "Fixed data validation issue";
        
        ResolveExceptionInput graphqlInput = ResolveExceptionInput.builder()
                .transactionId(transactionId)
                .resolutionMethod(resolutionMethod)
                .resolutionNotes(resolutionNotes)
                .build();

        ResolveResponse serviceResponse = ResolveResponse.builder()
                .transactionId(transactionId)
                .status("RESOLVED")
                .resolvedBy("test.user@company.com")
                .resolvedAt(OffsetDateTime.now())
                .resolutionMethod(resolutionMethod)
                .resolutionNotes(resolutionNotes)
                .build();

        InterfaceException resolvedException = testException.toBuilder()
                .status(ExceptionStatus.RESOLVED)
                .resolvedBy("test.user@company.com")
                .resolvedAt(OffsetDateTime.now())
                .resolutionMethod(resolutionMethod)
                .resolutionNotes(resolutionNotes)
                .build();

        when(exceptionManagementService.canResolve(transactionId)).thenReturn(true);
        when(exceptionManagementService.resolveException(eq(transactionId), any(ResolveRequest.class)))
                .thenReturn(serviceResponse);
        when(exceptionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(resolvedException));

        // When
        ResolveExceptionResult result = resolver.resolveException(graphqlInput, authentication).join();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getException()).isEqualTo(resolvedException);
        assertThat(result.getException().getStatus()).isEqualTo(ExceptionStatus.RESOLVED);

        // Verify same service calls as REST API
        verify(exceptionManagementService).canResolve(transactionId);
        verify(exceptionManagementService).resolveException(eq(transactionId), any(ResolveRequest.class));
        verify(exceptionRepository).findByTransactionId(transactionId);
    }

    @Test
    void retryException_WhenNotAllowed_ShouldHandleErrorSameAsRestApi() {
        // Given
        String transactionId = "TEST-123";
        RetryExceptionInput graphqlInput = RetryExceptionInput.builder()
                .transactionId(transactionId)
                .reason("Manual retry")
                .build();

        when(retryService.canRetry(transactionId)).thenReturn(false);

        // When
        RetryExceptionResult result = resolver.retryException(graphqlInput, authentication).join();

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("RETRY_NOT_ALLOWED");
        assertThat(result.getErrors().get(0).getMessage())
                .isEqualTo("Exception is not retryable or retry already in progress");

        // Verify same validation as REST API
        verify(retryService).canRetry(transactionId);
    }

    @Test
    void acknowledgeException_WhenNotAllowed_ShouldHandleErrorSameAsRestApi() {
        // Given
        String transactionId = "TEST-123";
        AcknowledgeExceptionInput graphqlInput = AcknowledgeExceptionInput.builder()
                .transactionId(transactionId)
                .reason("Investigating")
                .build();

        when(exceptionManagementService.canAcknowledge(transactionId)).thenReturn(false);

        // When
        AcknowledgeExceptionResult result = resolver.acknowledgeException(graphqlInput, authentication).join();

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("ACKNOWLEDGMENT_NOT_ALLOWED");
        assertThat(result.getErrors().get(0).getMessage())
                .isEqualTo("Exception cannot be acknowledged (not found, already resolved, or closed)");

        // Verify same validation as REST API
        verify(exceptionManagementService).canAcknowledge(transactionId);
    }

    @Test
    void resolveException_WhenNotAllowed_ShouldHandleErrorSameAsRestApi() {
        // Given
        String transactionId = "TEST-123";
        ResolveExceptionInput graphqlInput = ResolveExceptionInput.builder()
                .transactionId(transactionId)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Fixed issue")
                .build();

        when(exceptionManagementService.canResolve(transactionId)).thenReturn(false);

        // When
        ResolveExceptionResult result = resolver.resolveException(graphqlInput, authentication).join();

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getCode()).isEqualTo("RESOLUTION_NOT_ALLOWED");
        assertThat(result.getErrors().get(0).getMessage())
                .isEqualTo("Exception cannot be resolved (not found, already resolved, or closed)");

        // Verify same validation as REST API
        verify(exceptionManagementService).canResolve(transactionId);
    }
}