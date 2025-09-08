package com.arcone.biopro.exception.collector.api.rest;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionSubscriptionResolver;
import com.arcone.biopro.exception.collector.api.graphql.service.RetryEventBridge;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Test controller for triggering retry events to test GraphQL subscriptions.
 * This is for development/testing purposes only.
 */
@RestController
@RequestMapping("/api/test/retry")
@RequiredArgsConstructor
@Slf4j
public class TestRetryController {

    private final RetryEventBridge retryEventBridge;
    private final InterfaceExceptionRepository exceptionRepository;

    /**
     * Trigger a test retry event for subscription testing.
     */
    @PostMapping("/trigger/{transactionId}")
    public ResponseEntity<Map<String, Object>> triggerRetryEvent(
            @PathVariable String transactionId,
            @RequestParam(defaultValue = "INITIATED") String eventType) {
        
        log.info("🧪 Test retry event trigger requested for transaction: {}, eventType: {}", 
                transactionId, eventType);

        try {
            // Find the exception
            Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
            if (exceptionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Exception not found: " + transactionId
                ));
            }

            InterfaceException exception = exceptionOpt.get();

            // Create a mock retry attempt
            RetryAttempt mockRetryAttempt = new RetryAttempt();
            mockRetryAttempt.setId(System.currentTimeMillis()); // Mock ID
            mockRetryAttempt.setInterfaceException(exception);
            mockRetryAttempt.setAttemptNumber(exception.getRetryCount() + 1);
            mockRetryAttempt.setStatus(RetryStatus.PENDING);
            mockRetryAttempt.setInitiatedAt(OffsetDateTime.now());
            mockRetryAttempt.setInitiatedBy("test-user");
            // Note: RetryAttempt doesn't have a reason field, using resultMessage instead

            // Parse event type
            ExceptionSubscriptionResolver.RetryEventType retryEventType;
            try {
                retryEventType = ExceptionSubscriptionResolver.RetryEventType.valueOf(eventType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid event type: " + eventType + ". Valid types: INITIATED, IN_PROGRESS, COMPLETED, FAILED, CANCELLED"
                ));
            }

            // Publish the retry event
            retryEventBridge.publishRetryEvent(transactionId, mockRetryAttempt, retryEventType);

            log.info("✅ Test retry event published successfully for transaction: {}", transactionId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Retry event published successfully",
                "transactionId", transactionId,
                "eventType", retryEventType.toString(),
                "attemptNumber", mockRetryAttempt.getAttemptNumber(),
                "timestamp", OffsetDateTime.now().toString()
            ));

        } catch (Exception e) {
            log.error("❌ Failed to trigger test retry event for transaction: {}", transactionId, e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to trigger retry event: " + e.getMessage()
            ));
        }
    }

    /**
     * Trigger multiple retry events in sequence for testing.
     */
    @PostMapping("/sequence/{transactionId}")
    public ResponseEntity<Map<String, Object>> triggerRetrySequence(@PathVariable String transactionId) {
        
        log.info("🧪 Test retry sequence trigger requested for transaction: {}", transactionId);

        try {
            // Find the exception
            Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
            if (exceptionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Exception not found: " + transactionId
                ));
            }

            InterfaceException exception = exceptionOpt.get();

            // Create a mock retry attempt
            RetryAttempt mockRetryAttempt = new RetryAttempt();
            mockRetryAttempt.setId(System.currentTimeMillis());
            mockRetryAttempt.setInterfaceException(exception);
            mockRetryAttempt.setAttemptNumber(exception.getRetryCount() + 1);
            mockRetryAttempt.setStatus(RetryStatus.PENDING);
            mockRetryAttempt.setInitiatedAt(OffsetDateTime.now());
            mockRetryAttempt.setInitiatedBy("test-user");
            // Note: RetryAttempt doesn't have a reason field, using resultMessage instead

            // Trigger sequence of events with delays
            new Thread(() -> {
                try {
                    // 1. Initiated
                    retryEventBridge.publishRetryInitiated(transactionId, mockRetryAttempt);
                    Thread.sleep(2000);

                    // 2. In Progress (using PENDING since IN_PROGRESS doesn't exist)
                    mockRetryAttempt.setStatus(RetryStatus.PENDING);
                    retryEventBridge.publishRetryInProgress(transactionId, mockRetryAttempt);
                    Thread.sleep(3000);

                    // 3. Completed (or Failed randomly)
                    if (Math.random() > 0.5) {
                        mockRetryAttempt.setStatus(RetryStatus.SUCCESS);
                        retryEventBridge.publishRetryCompleted(transactionId, mockRetryAttempt);
                    } else {
                        mockRetryAttempt.setStatus(RetryStatus.FAILED);
                        retryEventBridge.publishRetryFailed(transactionId, mockRetryAttempt);
                    }

                } catch (Exception e) {
                    log.error("❌ Error in retry sequence for transaction: {}", transactionId, e);
                }
            }).start();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Retry sequence initiated - events will be published over 5 seconds",
                "transactionId", transactionId,
                "sequence", "INITIATED -> IN_PROGRESS -> (COMPLETED|FAILED)"
            ));

        } catch (Exception e) {
            log.error("❌ Failed to trigger retry sequence for transaction: {}", transactionId, e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to trigger retry sequence: " + e.getMessage()
            ));
        }
    }
}