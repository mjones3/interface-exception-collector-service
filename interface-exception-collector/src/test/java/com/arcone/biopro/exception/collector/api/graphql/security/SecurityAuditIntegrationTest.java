package com.arcone.biopro.exception.collector.api.graphql.security;

import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.resolver.RetryMutationResolver;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.MutationAuditLog;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.MutationAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for security audit logging functionality.
 * Tests the complete flow of rate limiting, operation tracking, and audit logging.
 * 
 * Requirements: 5.3, 5.5
 */
@SpringBootTest
@TestPropertySource(properties = {
    "app.security.rate-limit.mutations.per-minute=2",
    "app.security.rate-limit.mutations.per-hour=10",
    "app.security.audit.mutations.enabled=true",
    "app.security.operation-tracking.enabled=true"
})
@Transactional
class SecurityAuditIntegrationTest {

    @Autowired
    private RetryMutationResolver retryMutationResolver;

    @Autowired
    private MutationRateLimiter rateLimiter;

    @Autowired
    private OperationTracker operationTracker;

    @Autowired
    private SecurityAuditLogger auditLogger;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private MutationAuditLogRepository auditLogRepository;

    private Authentication testAuthentication;

    @BeforeEach
    void setUp() {
        // Clear rate limits and tracking data
        rateLimiter.clearAll();
        operationTracker.clearAll();
        
        // Create test authentication
        testAuthentication = new UsernamePasswordAuthenticationToken(
            "test-user", 
            "password", 
            List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // Create test exception
        InterfaceException testException = InterfaceException.builder()
            .transactionId("TEST-TXN-123")
            .status(InterfaceException.ExceptionStatus.FAILED)
            .errorMessage("Test error")
            .retryCount(0)
            .maxRetries(3)
            .createdAt(Instant.now())
            .build();
        
        exceptionRepository.save(testException);
    }

    @Test
    void retryException_ShouldCreateAuditLogAndTrackOperation() throws Exception {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
            .transactionId("TEST-TXN-123")
            .reason("Integration test retry")
            .build();

        // When
        CompletableFuture<RetryExceptionResult> resultFuture = 
            retryMutationResolver.retryException(input, testAuthentication);
        RetryExceptionResult result = resultFuture.get();

        // Then - Operation should succeed
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getErrors()).isEmpty();

        // Verify audit log was created
        List<MutationAuditLog> auditLogs = auditLogRepository.findByTransactionIdOrderByPerformedAtDesc("TEST-TXN-123");
        assertThat(auditLogs).hasSize(1);
        
        MutationAuditLog auditLog = auditLogs.get(0);
        assertThat(auditLog.getOperationType()).isEqualTo(MutationAuditLog.OperationType.RETRY);
        assertThat(auditLog.getPerformedBy()).isEqualTo("test-user");
        assertThat(auditLog.getResultStatus()).isEqualTo(MutationAuditLog.ResultStatus.SUCCESS);
        assertThat(auditLog.getExecutionTimeMs()).isNotNull();
        assertThat(auditLog.getExecutionTimeMs()).isGreaterThan(0);

        // Verify operation tracking
        OperationTracker.OperationStatsSummary operationStats = operationTracker.getOperationStats("RETRY");
        assertThat(operationStats).isNotNull();
        assertThat(operationStats.getTotalCount()).isEqualTo(1);
        assertThat(operationStats.getSuccessCount()).isEqualTo(1);
        assertThat(operationStats.getFailureCount()).isEqualTo(0);

        OperationTracker.UserOperationStatsSummary userStats = operationTracker.getUserStats("test-user");
        assertThat(userStats).isNotNull();
        assertThat(userStats.getTotalOperations()).isEqualTo(1);
        assertThat(userStats.getSuccessfulOperations()).isEqualTo(1);
    }

    @Test
    void retryException_ExceedingRateLimit_ShouldBeBlocked() throws Exception {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
            .transactionId("TEST-TXN-123")
            .reason("Rate limit test")
            .build();

        // When - Make requests up to the limit (2 per minute)
        CompletableFuture<RetryExceptionResult> result1 = 
            retryMutationResolver.retryException(input, testAuthentication);
        CompletableFuture<RetryExceptionResult> result2 = 
            retryMutationResolver.retryException(input, testAuthentication);
        CompletableFuture<RetryExceptionResult> result3 = 
            retryMutationResolver.retryException(input, testAuthentication);

        // Then - First two should succeed, third should be rate limited
        assertThat(result1.get().isSuccess()).isTrue();
        assertThat(result2.get().isSuccess()).isTrue();
        
        RetryExceptionResult result3Value = result3.get();
        assertThat(result3Value.isSuccess()).isFalse();
        assertThat(result3Value.getErrors()).hasSize(1);
        assertThat(result3Value.getErrors().get(0).getMessage()).contains("Rate limit exceeded");

        // Verify audit logs for all attempts
        List<MutationAuditLog> auditLogs = auditLogRepository.findByTransactionIdOrderByPerformedAtDesc("TEST-TXN-123");
        assertThat(auditLogs).hasSize(3);
        
        // First two should be successful
        assertThat(auditLogs.get(2).getResultStatus()).isEqualTo(MutationAuditLog.ResultStatus.SUCCESS);
        assertThat(auditLogs.get(1).getResultStatus()).isEqualTo(MutationAuditLog.ResultStatus.SUCCESS);
        
        // Third should be failed due to rate limit
        assertThat(auditLogs.get(0).getResultStatus()).isEqualTo(MutationAuditLog.ResultStatus.FAILURE);
        assertThat(auditLogs.get(0).getErrorDetails()).contains("Rate limit exceeded");
    }

    @Test
    void retryException_DifferentUsers_ShouldHaveSeparateRateLimits() throws Exception {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
            .transactionId("TEST-TXN-123")
            .reason("Multi-user test")
            .build();

        Authentication user2Auth = new UsernamePasswordAuthenticationToken(
            "test-user-2", 
            "password", 
            List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // When - Each user makes requests up to their limit
        CompletableFuture<RetryExceptionResult> user1Result1 = 
            retryMutationResolver.retryException(input, testAuthentication);
        CompletableFuture<RetryExceptionResult> user1Result2 = 
            retryMutationResolver.retryException(input, testAuthentication);
        
        CompletableFuture<RetryExceptionResult> user2Result1 = 
            retryMutationResolver.retryException(input, user2Auth);
        CompletableFuture<RetryExceptionResult> user2Result2 = 
            retryMutationResolver.retryException(input, user2Auth);

        // Then - All should succeed (separate limits)
        assertThat(user1Result1.get().isSuccess()).isTrue();
        assertThat(user1Result2.get().isSuccess()).isTrue();
        assertThat(user2Result1.get().isSuccess()).isTrue();
        assertThat(user2Result2.get().isSuccess()).isTrue();

        // Verify separate user statistics
        OperationTracker.UserOperationStatsSummary user1Stats = operationTracker.getUserStats("test-user");
        OperationTracker.UserOperationStatsSummary user2Stats = operationTracker.getUserStats("test-user-2");
        
        assertThat(user1Stats.getTotalOperations()).isEqualTo(2);
        assertThat(user2Stats.getTotalOperations()).isEqualTo(2);
        
        // Verify separate audit logs
        List<MutationAuditLog> user1Logs = auditLogRepository.findByPerformedByOrderByPerformedAtDesc("test-user", null);
        List<MutationAuditLog> user2Logs = auditLogRepository.findByPerformedByOrderByPerformedAtDesc("test-user-2", null);
        
        assertThat(user1Logs).hasSize(2);
        assertThat(user2Logs).hasSize(2);
    }

    @Test
    void rateLimitStatus_ShouldReflectCurrentUsage() {
        // Given
        String userId = "test-user";
        String operationType = "RETRY";

        // When - Make one request
        rateLimiter.checkRateLimit(userId, operationType);
        
        MutationRateLimiter.RateLimitStatus status = rateLimiter.getRateLimitStatus(userId, operationType);

        // Then
        assertThat(status.getCurrentMinuteCount()).isEqualTo(1);
        assertThat(status.getMaxMinuteCount()).isEqualTo(2);
        assertThat(status.getCurrentHourCount()).isEqualTo(1);
        assertThat(status.getMaxHourCount()).isEqualTo(10);
        assertThat(status.isMinuteLimitExceeded()).isFalse();
        assertThat(status.isHourLimitExceeded()).isFalse();
    }

    @Test
    void globalOperationStats_ShouldAggregateAllOperations() throws Exception {
        // Given
        RetryExceptionInput input = RetryExceptionInput.builder()
            .transactionId("TEST-TXN-123")
            .reason("Global stats test")
            .build();

        // When - Make multiple operations
        retryMutationResolver.retryException(input, testAuthentication).get();
        retryMutationResolver.retryException(input, testAuthentication).get();

        // Then
        OperationTracker.GlobalStatsSummary globalStats = operationTracker.getGlobalStats();
        assertThat(globalStats.getTotalOperations()).isEqualTo(2);
        assertThat(globalStats.getTotalSuccessfulOperations()).isEqualTo(2);
        assertThat(globalStats.getTotalFailedOperations()).isEqualTo(0);
        assertThat(globalStats.getUniqueUsers()).isEqualTo(1);
        assertThat(globalStats.getUniqueOperationTypes()).isEqualTo(1);
        assertThat(globalStats.getGlobalSuccessRate()).isEqualTo(1.0);
    }
}