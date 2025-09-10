package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionResult;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for simplified acknowledge mutation functionality.
 * Tests the complete flow from GraphQL input to database persistence.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AcknowledgeMutationIntegrationTest {

    @Autowired
    private RetryMutationResolver retryMutationResolver;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    private Authentication testAuthentication;
    private InterfaceException testException;

    @BeforeEach
    void setUp() {
        // Setup test authentication
        testAuthentication = new UsernamePasswordAuthenticationToken(
                "test.user",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // Create test exception
        testException = InterfaceException.builder()
                .transactionId("TEST-TXN-123")
                .interfaceType(InterfaceType.ORDER_PROCESSING)
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.HIGH)
                .errorMessage("Test error message")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryable(true)
                .build();

        exceptionRepository.save(testException);
    }

    @Test
    void acknowledgeException_WithSimplifiedInput_ShouldSucceed() throws Exception {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TEST-TXN-123")
                .reason("Test acknowledgment reason")
                .notes("Test acknowledgment notes")
                .build();

        // When
        CompletableFuture<AcknowledgeExceptionResult> resultFuture = 
                retryMutationResolver.acknowledgeException(input, testAuthentication);
        AcknowledgeExceptionResult result = resultFuture.get();

        // Then
        assertTrue(result.isSuccess(), "Acknowledgment should succeed");
        assertNotNull(result.getException(), "Result should contain exception");
        assertEquals(ExceptionStatus.ACKNOWLEDGED, result.getException().getStatus());
        assertEquals("test.user", result.getException().getAcknowledgedBy());
        assertNotNull(result.getException().getAcknowledgedAt());
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");

        // Verify database state
        InterfaceException updatedException = exceptionRepository
                .findByTransactionId("TEST-TXN-123")
                .orElseThrow();
        assertEquals(ExceptionStatus.ACKNOWLEDGED, updatedException.getStatus());
        assertEquals("test.user", updatedException.getAcknowledgedBy());
        assertNotNull(updatedException.getAcknowledgedAt());
    }

    @Test
    void acknowledgeException_WithMinimalInput_ShouldSucceed() throws Exception {
        // Given - Only required fields
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TEST-TXN-123")
                .reason("Minimal acknowledgment")
                .build();

        // When
        CompletableFuture<AcknowledgeExceptionResult> resultFuture = 
                retryMutationResolver.acknowledgeException(input, testAuthentication);
        AcknowledgeExceptionResult result = resultFuture.get();

        // Then
        assertTrue(result.isSuccess(), "Acknowledgment should succeed with minimal input");
        assertNotNull(result.getException(), "Result should contain exception");
        assertEquals(ExceptionStatus.ACKNOWLEDGED, result.getException().getStatus());
    }

    @Test
    void acknowledgeException_WithNonExistentTransaction_ShouldFail() throws Exception {
        // Given
        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("NON-EXISTENT-TXN")
                .reason("Test acknowledgment reason")
                .build();

        // When
        CompletableFuture<AcknowledgeExceptionResult> resultFuture = 
                retryMutationResolver.acknowledgeException(input, testAuthentication);
        AcknowledgeExceptionResult result = resultFuture.get();

        // Then
        assertFalse(result.isSuccess(), "Acknowledgment should fail for non-existent transaction");
        assertFalse(result.getErrors().isEmpty(), "Should have errors");
    }

    @Test
    void acknowledgeException_WithResolvedStatus_ShouldFail() throws Exception {
        // Given - Exception already resolved
        testException.setStatus(ExceptionStatus.RESOLVED);
        exceptionRepository.save(testException);

        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TEST-TXN-123")
                .reason("Test acknowledgment reason")
                .build();

        // When
        CompletableFuture<AcknowledgeExceptionResult> resultFuture = 
                retryMutationResolver.acknowledgeException(input, testAuthentication);
        AcknowledgeExceptionResult result = resultFuture.get();

        // Then
        assertFalse(result.isSuccess(), "Acknowledgment should fail for resolved exception");
        assertFalse(result.getErrors().isEmpty(), "Should have errors");
    }

    @Test
    void acknowledgeException_ReAcknowledgment_ShouldSucceed() throws Exception {
        // Given - Exception already acknowledged
        testException.setStatus(ExceptionStatus.ACKNOWLEDGED);
        testException.setAcknowledgedBy("previous.user");
        testException.setAcknowledgedAt(OffsetDateTime.now().minusHours(1));
        exceptionRepository.save(testException);

        AcknowledgeExceptionInput input = AcknowledgeExceptionInput.builder()
                .transactionId("TEST-TXN-123")
                .reason("Re-acknowledgment with updated notes")
                .notes("Updated acknowledgment notes")
                .build();

        // When
        CompletableFuture<AcknowledgeExceptionResult> resultFuture = 
                retryMutationResolver.acknowledgeException(input, testAuthentication);
        AcknowledgeExceptionResult result = resultFuture.get();

        // Then
        assertTrue(result.isSuccess(), "Re-acknowledgment should succeed");
        assertNotNull(result.getException(), "Result should contain exception");
        assertEquals("test.user", result.getException().getAcknowledgedBy());
    }
}