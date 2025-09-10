package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.dto.AcknowledgeRequest;
import com.arcone.biopro.exception.collector.api.dto.AcknowledgeResponse;
import com.arcone.biopro.exception.collector.api.dto.ResolveRequest;
import com.arcone.biopro.exception.collector.api.dto.ResolveResponse;
import com.arcone.biopro.exception.collector.api.dto.RetryRequest;
import com.arcone.biopro.exception.collector.api.dto.RetryResponse;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkRetryInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkRetryResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.CancelRetryResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkAcknowledgeInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkAcknowledgeResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError;
import com.arcone.biopro.exception.collector.api.graphql.validation.GraphQLErrorHandler;
import com.arcone.biopro.exception.collector.api.graphql.validation.MutationErrorCode;
import com.arcone.biopro.exception.collector.api.graphql.service.RetryValidationService;
import com.arcone.biopro.exception.collector.api.graphql.service.AcknowledgmentValidationService;
import com.arcone.biopro.exception.collector.api.graphql.service.ResolutionValidationService;
import com.arcone.biopro.exception.collector.api.graphql.service.CancelRetryValidationService;
import com.arcone.biopro.exception.collector.api.graphql.validation.ValidationResult;
import com.arcone.biopro.exception.collector.api.graphql.security.SecurityAuditLogger;
import com.arcone.biopro.exception.collector.api.graphql.security.MutationRateLimiter;
import com.arcone.biopro.exception.collector.api.graphql.security.OperationTracker;
import com.arcone.biopro.exception.collector.api.graphql.security.RateLimitExceededException;
import com.arcone.biopro.exception.collector.application.service.RetryService;
import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.entity.MutationAuditLog;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import com.arcone.biopro.exception.collector.infrastructure.monitoring.MutationMetrics;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL resolver for exception management mutations.
 * Handles retry, acknowledgment, and resolution operations using existing
 * business services.
 * Ensures consistency with REST API endpoints by delegating to the same service
 * layer.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RetryMutationResolver {

        private final RetryService retryService;
        private final ExceptionManagementService exceptionManagementService;
        private final RetryValidationService validationService;
        private final AcknowledgmentValidationService acknowledgmentValidationService;
        private final ResolutionValidationService resolutionValidationService;
        private final CancelRetryValidationService cancelRetryValidationService;
        private final SecurityAuditLogger auditLogger;
        private final MutationRateLimiter rateLimiter;
        private final OperationTracker operationTracker;
        private final InterfaceExceptionRepository exceptionRepository;
        private final RetryAttemptRepository retryAttemptRepository;
        private final com.arcone.biopro.exception.collector.api.graphql.service.MutationEventPublisher mutationEventPublisher;
        private final MutationMetrics mutationMetrics;

        /**
         * Initiates a retry operation for a single exception.
         * Uses the same business logic as the REST API endpoint.
         *
         * @param input          the retry input containing transaction ID and retry
         *                       details
         * @param authentication the current user authentication
         * @return CompletableFuture containing the retry result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<RetryExceptionResult> retryException(
                        @Argument RetryExceptionInput input,
                        Authentication authentication) {

                log.info("GraphQL retry exception requested for transaction: {} by user: {}",
                                input.getTransactionId(), authentication.getName());

                return CompletableFuture.supplyAsync(() -> {
                        long startTime = System.currentTimeMillis();
                        Timer.Sample metricsTimer = mutationMetrics.startRetryOperation();
                        String operationId = auditLogger.generateOperationId("retry", input.getTransactionId());
                        String correlationId = auditLogger.generateCorrelationId();
                        String trackingId = null;
                        
                        try {
                                // Check rate limits before processing
                                rateLimiter.checkRateLimit(authentication.getName(), "RETRY");
                                
                                // Start operation tracking
                                trackingId = operationTracker.recordOperationStart("RETRY", 
                                        authentication.getName(), input.getTransactionId());
                                
                                // Log mutation attempt with comprehensive audit information
                                auditLogger.logMutationAttempt(
                                        MutationAuditLog.OperationType.RETRY,
                                        input.getTransactionId(),
                                        authentication.getName(),
                                        input,
                                        operationId,
                                        correlationId
                                );
                        } catch (RateLimitExceededException e) {
                                log.warn("Rate limit exceeded for user {} on retry operation: {}", 
                                        authentication.getName(), e.getMessage());
                                
                                com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError error = 
                                        com.arcone.biopro.exception.collector.api.graphql.validation.GraphQLErrorHandler
                                                .createRateLimitError(e);
                                
                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);
                                mutationMetrics.recordRetryOperation(metricsTimer, false, "RATE_LIMIT_EXCEEDED");
                                
                                return RetryExceptionResult.builder()
                                        .success(false)
                                        .errors(List.of(error))
                                        .build();
                        }

                        try {
                                // Enhanced validation with detailed error categorization
                                ValidationResult validation = validationService.validateRetryOperation(input, authentication);
                                if (!validation.isValid()) {
                                        log.warn("Retry validation failed for transaction: {} with {} errors", 
                                                input.getTransactionId(), validation.getErrorCount());
                                        
                                        long executionTime = System.currentTimeMillis() - startTime;
                                        auditLogger.logMutationResult(operationId, false, 
                                                List.of(validation.getErrors()), executionTime);
                                        
                                        mutationMetrics.recordRetryOperation(metricsTimer, false, "VALIDATION_ERROR");
                                        
                                        // Record operation completion in tracker
                                        if (trackingId != null) {
                                                operationTracker.recordOperationComplete(trackingId, "RETRY", 
                                                        authentication.getName(), false, executionTime);
                                        }
                                        
                                        return RetryExceptionResult.builder()
                                                        .success(false)
                                                        .errors(validation.getErrors())
                                                        .build();
                                }

                                // Convert GraphQL input to service request (same as REST API)
                                RetryRequest retryRequest = RetryRequest.builder()
                                                .reason(input.getReason())
                                                .initiatedBy(authentication.getName())
                                                .build();

                                // Execute retry through existing service (same as REST API)
                                RetryResponse retryResponse = retryService.initiateRetry(input.getTransactionId(),
                                                retryRequest);

                                // Fetch updated exception and retry attempt
                                InterfaceException exception = exceptionRepository
                                                .findByTransactionId(input.getTransactionId())
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "Exception not found after retry initiation"));

                                RetryAttempt retryAttempt = retryAttemptRepository.findById(retryResponse.getRetryId())
                                                .orElse(null);

                                log.info("GraphQL retry exception completed for transaction: {}, retry ID: {}",
                                                input.getTransactionId(), retryResponse.getRetryId());

                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, true, List.of(), executionTime);

                                mutationMetrics.recordRetryOperation(metricsTimer, true, null);
                                
                                // Record operation completion in tracker
                                if (trackingId != null) {
                                        operationTracker.recordOperationComplete(trackingId, "RETRY", 
                                                authentication.getName(), true, executionTime);
                                }

                                // Publish mutation completion event for real-time subscription updates
                                if (retryAttempt != null) {
                                        mutationEventPublisher.publishRetryMutationCompleted(
                                                exception, retryAttempt, true, authentication.getName());
                                }

                                return RetryExceptionResult.builder()
                                                .success(true)
                                                .exception(exception)
                                                .retryAttempt(retryAttempt)
                                                .errors(List.of())
                                                .build();

                        } catch (IllegalArgumentException e) {
                                log.warn("Retry operation failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage());

                                com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError error = 
                                        com.arcone.biopro.exception.collector.api.graphql.validation.GraphQLErrorHandler
                                                .createFromException(e, "retry");

                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);

                                mutationMetrics.recordRetryOperation(metricsTimer, false, "BUSINESS_RULE_ERROR");
                                
                                // Record operation completion in tracker
                                if (trackingId != null) {
                                        operationTracker.recordOperationComplete(trackingId, "RETRY", 
                                                authentication.getName(), false, executionTime);
                                }

                                return RetryExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(error))
                                                .build();

                        } catch (Exception e) {
                                log.error("GraphQL retry exception failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage(), e);

                                com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError error = 
                                        com.arcone.biopro.exception.collector.api.graphql.validation.GraphQLErrorHandler
                                                .createFromException(e, "retry");

                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);

                                mutationMetrics.recordRetryOperation(metricsTimer, false, "SYSTEM_ERROR");
                                
                                // Record operation completion in tracker
                                if (trackingId != null) {
                                        operationTracker.recordOperationComplete(trackingId, "RETRY", 
                                                authentication.getName(), false, executionTime);
                                }

                                return RetryExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(error))
                                                .build();
                        }
                });
        }

        /**
         * Initiates retry operations for multiple exceptions in bulk.
         * Processes each retry individually using the same business logic as REST API.
         *
         * @param input          the bulk retry input containing transaction IDs and
         *                       retry details
         * @param authentication the current user authentication
         * @return CompletableFuture containing the bulk retry result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<BulkRetryResult> bulkRetryExceptions(
                        @Argument BulkRetryInput input,
                        Authentication authentication) {

                log.info("GraphQL bulk retry requested for {} exceptions by user: {}",
                                input.getTransactionIds().size(), authentication.getName());

                return CompletableFuture.supplyAsync(() -> {
                        long startTime = System.currentTimeMillis();
                        String operationId = auditLogger.generateOperationId("bulk_retry", "multiple");
                        String correlationId = auditLogger.generateCorrelationId();
                        
                        // Log bulk mutation attempt
                        auditLogger.logMutationAttempt(
                                MutationAuditLog.OperationType.BULK_RETRY,
                                "BULK_" + input.getTransactionIds().size() + "_TRANSACTIONS",
                                authentication.getName(),
                                input,
                                operationId,
                                correlationId
                        );

                        List<RetryExceptionResult> results = new ArrayList<>();
                        int successCount = 0;
                        int failureCount = 0;

                        for (String transactionId : input.getTransactionIds()) {
                                try {
                                        RetryExceptionInput singleInput = RetryExceptionInput.builder()
                                                        .transactionId(transactionId)
                                                        .reason(input.getReason())
                                                        .priority(input.getPriority())
                                                        .build();

                                        // Process each retry using the same logic as single retry
                                        RetryExceptionResult result = retryException(singleInput, authentication)
                                                        .join();
                                        results.add(result);

                                        if (result.isSuccess()) {
                                                successCount++;
                                        } else {
                                                failureCount++;
                                        }

                                } catch (Exception e) {
                                        log.error("Bulk retry failed for transaction: {}, error: {}",
                                                        transactionId, e.getMessage());

                                        RetryExceptionResult failedResult = RetryExceptionResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Failed to process retry for transaction: "
                                                                                        + transactionId)
                                                                        .code("BULK_RETRY_ITEM_FAILED")
                                                                        .build()))
                                                        .build();

                                        results.add(failedResult);
                                        failureCount++;
                                }
                        }

                        log.info("GraphQL bulk retry completed: {} successful, {} failed", successCount, failureCount);

                        long executionTime = System.currentTimeMillis() - startTime;
                        auditLogger.logBulkMutationResult(operationId, successCount, failureCount, 
                                List.of(), executionTime);

                        return BulkRetryResult.builder()
                                        .successCount(successCount)
                                        .failureCount(failureCount)
                                        .results(results)
                                        .errors(List.of())
                                        .build();
                });
        }

        /**
         * Cancels a pending retry operation with enhanced validation and error handling.
         * Provides better error messages for cancellation failures, validates retry state,
         * and implements proper concurrent operation handling.
         *
         * @param transactionId  the transaction ID of the exception
         * @param reason         the reason for cancellation
         * @param authentication the current user authentication
         * @return CompletableFuture containing the enhanced cancel result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<CancelRetryResult> cancelRetry(
                        @Argument String transactionId,
                        @Argument String reason,
                        Authentication authentication) {

                log.info("GraphQL cancel retry requested for transaction: {} by user: {} with reason: {}",
                                transactionId, authentication.getName(), reason);

                return CompletableFuture.supplyAsync(() -> {
                        long startTime = System.currentTimeMillis();
                        Timer.Sample metricsTimer = mutationMetrics.startCancelRetryOperation();
                        String operationId = auditLogger.generateOperationId("cancel", transactionId);
                        String correlationId = auditLogger.generateCorrelationId();
                        String performedBy = authentication.getName();

                        // Log mutation attempt
                        Map<String, Object> cancelInput = Map.of(
                                "transactionId", transactionId,
                                "reason", reason
                        );
                        auditLogger.logMutationAttempt(
                                MutationAuditLog.OperationType.CANCEL_RETRY,
                                transactionId,
                                performedBy,
                                cancelInput,
                                operationId,
                                correlationId
                        );

                        try {
                                // Enhanced validation with detailed error categorization
                                ValidationResult validation = cancelRetryValidationService.validateCancelRetryOperation(
                                        transactionId, reason, authentication);
                                
                                if (!validation.isValid()) {
                                        log.warn("Cancel retry validation failed for transaction: {} with {} errors", 
                                                transactionId, validation.getErrorCount());
                                        
                                        long executionTime = System.currentTimeMillis() - startTime;
                                        auditLogger.logMutationResult(operationId, false, 
                                                List.of(validation.getErrors()), executionTime);
                                        
                                        mutationMetrics.recordCancelRetryOperation(metricsTimer, false, "VALIDATION_ERROR");
                                        
                                        return CancelRetryResult.failure(validation.getErrors(), operationId, performedBy);
                                }

                                // Use available cancellation method
                                boolean cancelResult = retryService.cancelRetry(transactionId, 1); // Use attempt number 1 as default

                                if (cancelResult) {
                                        log.info("GraphQL cancel retry completed successfully for transaction: {}, operation: {}",
                                                        transactionId, operationId);

                                        long executionTime = System.currentTimeMillis() - startTime;
                                        auditLogger.logMutationResult(operationId, true, List.of(), executionTime);

                                        mutationMetrics.recordCancelRetryOperation(metricsTimer, true, null);

                                        // Create a simple success result without detailed exception/attempt info
                                        return CancelRetryResult.success(
                                                null, // exception not available from boolean result
                                                null, // cancelled attempt not available from boolean result
                                                operationId,
                                                performedBy,
                                                reason
                                        );
                                } else {
                                        log.warn("Cancel retry operation failed for transaction: {}, operation: {}",
                                                        transactionId, operationId);

                                        GraphQLError error = GraphQLErrorHandler.createBusinessRuleError(
                                                MutationErrorCode.CANCELLATION_FAILED,
                                                "Failed to cancel retry operation"
                                        );
                                        
                                        long executionTime = System.currentTimeMillis() - startTime;
                                        auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);
                                        
                                        mutationMetrics.recordCancelRetryOperation(metricsTimer, false, "BUSINESS_RULE_ERROR");
                                        
                                        return CancelRetryResult.failure(error, operationId, performedBy);
                                }

                        } catch (IllegalArgumentException e) {
                                log.warn("Cancel retry validation failed for transaction: {}, error: {}, operation: {}",
                                                transactionId, e.getMessage(), operationId);

                                GraphQLError error = GraphQLErrorHandler.createBusinessRuleError(
                                        MutationErrorCode.EXCEPTION_NOT_FOUND,
                                        e.getMessage()
                                );
                                
                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);
                                
                                mutationMetrics.recordCancelRetryOperation(metricsTimer, false, "BUSINESS_RULE_ERROR");
                                
                                return CancelRetryResult.failure(error, operationId, performedBy);

                        } catch (SecurityException e) {
                                log.warn("Cancel retry security error for transaction: {}, error: {}, operation: {}",
                                                transactionId, e.getMessage(), operationId);

                                GraphQLError error = GraphQLErrorHandler.createSecurityError(
                                        MutationErrorCode.INSUFFICIENT_PERMISSIONS,
                                        e.getMessage()
                                );
                                
                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);
                                
                                mutationMetrics.recordCancelRetryOperation(metricsTimer, false, "SECURITY_ERROR");
                                
                                return CancelRetryResult.failure(error, operationId, performedBy);

                        } catch (Exception e) {
                                log.error("GraphQL cancel retry failed for transaction: {}, error: {}, operation: {}",
                                                transactionId, e.getMessage(), operationId, e);

                                GraphQLError error = GraphQLErrorHandler.createSystemError(
                                        MutationErrorCode.DATABASE_ERROR,
                                        "Cancel retry operation failed: " + e.getMessage()
                                );
                                
                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);
                                
                                mutationMetrics.recordCancelRetryOperation(metricsTimer, false, "SYSTEM_ERROR");
                                
                                return CancelRetryResult.failure(error, operationId, performedBy);
                        }
                });
        }

        /**
         * Acknowledges a single exception with proper validation and audit logging.
         * Uses the same business logic as the REST API endpoint.
         *
         * @param input          the acknowledgment input containing transaction ID and
         *                       acknowledgment details
         * @param authentication the current user authentication
         * @return CompletableFuture containing the acknowledgment result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<AcknowledgeExceptionResult> acknowledgeException(
                        @Argument AcknowledgeExceptionInput input,
                        Authentication authentication) {

                log.info("GraphQL acknowledge exception requested for transaction: {} by user: {}",
                                input.getTransactionId(), authentication.getName());

                return CompletableFuture.supplyAsync(() -> {
                        long startTime = System.currentTimeMillis();
                        Timer.Sample metricsTimer = mutationMetrics.startAcknowledgeOperation();
                        String operationId = auditLogger.generateOperationId("acknowledge", input.getTransactionId());
                        String correlationId = auditLogger.generateCorrelationId();
                        
                        // Log mutation attempt
                        auditLogger.logMutationAttempt(
                                MutationAuditLog.OperationType.ACKNOWLEDGE,
                                input.getTransactionId(),
                                authentication.getName(),
                                input,
                                operationId,
                                correlationId
                        );

                        try {
                                // Enhanced validation with detailed error categorization
                                ValidationResult validation = acknowledgmentValidationService.validateAcknowledgmentOperation(input, authentication);
                                if (!validation.isValid()) {
                                        log.warn("Acknowledgment validation failed for transaction: {} with {} errors", 
                                                input.getTransactionId(), validation.getErrorCount());
                                        
                                        long executionTime = System.currentTimeMillis() - startTime;
                                        auditLogger.logMutationResult(operationId, false, 
                                                List.of(validation.getErrors()), executionTime);
                                        
                                        mutationMetrics.recordAcknowledgeOperation(metricsTimer, false, "VALIDATION_ERROR");
                                        
                                        return AcknowledgeExceptionResult.builder()
                                                        .success(false)
                                                        .errors(validation.getErrors())
                                                        .build();
                                }

                                // Convert GraphQL input to service request (same as REST API)
                                AcknowledgeRequest acknowledgeRequest = AcknowledgeRequest.builder()
                                                .acknowledgedBy(authentication.getName())
                                                .notes(buildAcknowledgmentNotes(input))
                                                .build();

                                // Execute acknowledgment through existing service (same as REST API)
                                AcknowledgeResponse acknowledgeResponse = exceptionManagementService
                                                .acknowledgeException(input.getTransactionId(), acknowledgeRequest);

                                // Fetch updated exception
                                InterfaceException exception = exceptionRepository
                                                .findByTransactionId(input.getTransactionId())
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "Exception not found after acknowledgment"));

                                log.info("GraphQL acknowledge exception completed for transaction: {}, acknowledged by: {}, at: {}",
                                                input.getTransactionId(), acknowledgeResponse.getAcknowledgedBy(),
                                                acknowledgeResponse.getAcknowledgedAt());

                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, true, List.of(), executionTime);

                                mutationMetrics.recordAcknowledgeOperation(metricsTimer, true, null);

                                // Publish mutation completion event for real-time subscription updates
                                mutationEventPublisher.publishAcknowledgeMutationCompleted(exception, authentication.getName());

                                return AcknowledgeExceptionResult.builder()
                                                .success(true)
                                                .exception(exception)
                                                .errors(List.of())
                                                .build();

                        } catch (IllegalArgumentException e) {
                                log.warn("Acknowledgment operation failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage());

                                GraphQLError error = GraphQLErrorHandler.createFromException(e, "acknowledge");
                                
                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);

                                mutationMetrics.recordAcknowledgeOperation(metricsTimer, false, "BUSINESS_RULE_ERROR");

                                return AcknowledgeExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(error))
                                                .build();

                        } catch (Exception e) {
                                log.error("GraphQL acknowledge exception failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage(), e);

                                GraphQLError error = GraphQLErrorHandler.createFromException(e, "acknowledge");
                                
                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);

                                mutationMetrics.recordAcknowledgeOperation(metricsTimer, false, "SYSTEM_ERROR");

                                return AcknowledgeExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(error))
                                                .build();
                        }
                });
        }

        /**
         * Acknowledges multiple exceptions in bulk with individual error handling.
         * Processes each acknowledgment individually using the same business logic as
         * REST API.
         *
         * @param input          the bulk acknowledgment input containing transaction
         *                       IDs and acknowledgment details
         * @param authentication the current user authentication
         * @return CompletableFuture containing the bulk acknowledgment result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<BulkAcknowledgeResult> bulkAcknowledgeExceptions(
                        @Argument BulkAcknowledgeInput input,
                        Authentication authentication) {

                log.info("GraphQL bulk acknowledge requested for {} exceptions by user: {}",
                                input.getTransactionIds().size(), authentication.getName());

                return CompletableFuture.supplyAsync(() -> {
                        long startTime = System.currentTimeMillis();
                        String operationId = auditLogger.generateOperationId("bulk_acknowledge", "multiple");
                        String correlationId = auditLogger.generateCorrelationId();
                        
                        // Log bulk mutation attempt
                        auditLogger.logMutationAttempt(
                                MutationAuditLog.OperationType.BULK_ACKNOWLEDGE,
                                "BULK_" + input.getTransactionIds().size() + "_TRANSACTIONS",
                                authentication.getName(),
                                input,
                                operationId,
                                correlationId
                        );

                        List<AcknowledgeExceptionResult> results = new ArrayList<>();
                        int successCount = 0;
                        int failureCount = 0;

                        for (String transactionId : input.getTransactionIds()) {
                                try {
                                        AcknowledgeExceptionInput singleInput = AcknowledgeExceptionInput.builder()
                                                        .transactionId(transactionId)
                                                        .reason(input.getReason())
                                                        .notes(input.getNotes())
                                                        .build();

                                        // Process each acknowledgment using the same logic as single acknowledgment
                                        AcknowledgeExceptionResult result = acknowledgeException(singleInput,
                                                        authentication).join();
                                        results.add(result);

                                        if (result.isSuccess()) {
                                                successCount++;
                                        } else {
                                                failureCount++;
                                        }

                                } catch (Exception e) {
                                        log.error("Bulk acknowledgment failed for transaction: {}, error: {}",
                                                        transactionId, e.getMessage());

                                        AcknowledgeExceptionResult failedResult = AcknowledgeExceptionResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Failed to process acknowledgment for transaction: "
                                                                                        + transactionId)
                                                                        .code("BULK_ACKNOWLEDGMENT_ITEM_FAILED")
                                                                        .build()))
                                                        .build();

                                        results.add(failedResult);
                                        failureCount++;
                                }
                        }

                        log.info("GraphQL bulk acknowledge completed: {} successful, {} failed", successCount,
                                        failureCount);

                        long executionTime = System.currentTimeMillis() - startTime;
                        auditLogger.logBulkMutationResult(operationId, successCount, failureCount, 
                                List.of(), executionTime);

                        return BulkAcknowledgeResult.builder()
                                        .successCount(successCount)
                                        .failureCount(failureCount)
                                        .results(results)
                                        .errors(List.of())
                                        .build();
                });
        }

        /**
         * Resolves a single exception with enhanced validation, proper state transition checking,
         * and streamlined resolution logic. Uses enhanced business rule validation and
         * improved error handling for invalid resolution attempts.
         *
         * @param input          the resolution input containing transaction ID and
         *                       resolution details
         * @param authentication the current user authentication
         * @return CompletableFuture containing the resolution result with enhanced metadata
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<ResolveExceptionResult> resolveException(
                        @Argument ResolveExceptionInput input,
                        Authentication authentication) {

                log.info("GraphQL resolve exception requested for transaction: {} by user: {} with method: {}",
                                input.getTransactionId(), authentication.getName(), input.getResolutionMethod());

                return CompletableFuture.supplyAsync(() -> {
                        long startTime = System.currentTimeMillis();
                        Timer.Sample metricsTimer = mutationMetrics.startResolveOperation();
                        String operationId = auditLogger.generateOperationId("resolve", input.getTransactionId());
                        String correlationId = auditLogger.generateCorrelationId();
                        String performedBy = authentication.getName();

                        // Log mutation attempt
                        auditLogger.logMutationAttempt(
                                MutationAuditLog.OperationType.RESOLVE,
                                input.getTransactionId(),
                                performedBy,
                                input,
                                operationId,
                                correlationId
                        );

                        try {
                                // Enhanced validation with detailed error categorization and state transition checking
                                ValidationResult validation = resolutionValidationService.validateResolutionOperation(input, authentication);
                                if (!validation.isValid()) {
                                        log.warn("Resolution validation failed for transaction: {} with {} errors", 
                                                input.getTransactionId(), validation.getErrorCount());
                                        
                                        long executionTime = System.currentTimeMillis() - startTime;
                                        auditLogger.logMutationResult(operationId, false, 
                                                List.of(validation.getErrors()), executionTime);
                                        
                                        mutationMetrics.recordResolveOperation(metricsTimer, false, "VALIDATION_ERROR");
                                        
                                        return ResolveExceptionResult.failure(validation.getErrors(), operationId, performedBy);
                                }

                                // Additional business rule check using existing service for consistency
                                if (!exceptionManagementService.canResolve(input.getTransactionId())) {
                                        log.warn("Exception cannot be resolved - transaction: {}, status check failed",
                                                        input.getTransactionId());
                                        
                                        GraphQLError error = GraphQLErrorHandler.createBusinessRuleError(
                                                MutationErrorCode.RESOLUTION_NOT_ALLOWED,
                                                "Exception cannot be resolved (not found, already resolved, or in invalid state)"
                                        );
                                        
                                        long executionTime = System.currentTimeMillis() - startTime;
                                        auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);
                                        
                                        mutationMetrics.recordResolveOperation(metricsTimer, false, "BUSINESS_RULE_ERROR");
                                        
                                        return ResolveExceptionResult.failure(error, operationId, performedBy);
                                }

                                // Convert GraphQL input to service request with enhanced data handling
                                ResolveRequest resolveRequest = ResolveRequest.builder()
                                                .resolvedBy(performedBy)
                                                .resolutionMethod(input.getResolutionMethod())
                                                .resolutionNotes(input.getTrimmedResolutionNotes())
                                                .build();

                                // Execute resolution through existing service (same as REST API)
                                ResolveResponse resolveResponse = exceptionManagementService
                                                .resolveException(input.getTransactionId(), resolveRequest);

                                // Fetch updated exception with error handling
                                InterfaceException exception = exceptionRepository
                                                .findByTransactionId(input.getTransactionId())
                                                .orElseThrow(() -> new IllegalStateException(
                                                                "Exception not found after resolution - possible concurrent modification"));

                                log.info("GraphQL resolve exception completed for transaction: {}, resolved by: {}, method: {}, at: {}, operation: {}",
                                                input.getTransactionId(), resolveResponse.getResolvedBy(),
                                                resolveResponse.getResolutionMethod(), resolveResponse.getResolvedAt(), operationId);

                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, true, List.of(), executionTime);

                                mutationMetrics.recordResolveOperation(metricsTimer, true, null);

                                // Publish mutation completion event for real-time subscription updates
                                mutationEventPublisher.publishResolveMutationCompleted(exception, performedBy);

                                // Return enhanced result with operation metadata
                                return ResolveExceptionResult.success(
                                        exception,
                                        operationId,
                                        performedBy,
                                        input.getResolutionMethod(),
                                        input.getTrimmedResolutionNotes()
                                );

                        } catch (IllegalArgumentException e) {
                                log.warn("Resolution validation failed for transaction: {}, error: {}, operation: {}",
                                                input.getTransactionId(), e.getMessage(), operationId);

                                GraphQLError error = GraphQLErrorHandler.createBusinessRuleError(
                                        MutationErrorCode.EXCEPTION_NOT_FOUND,
                                        e.getMessage()
                                );
                                
                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);
                                
                                mutationMetrics.recordResolveOperation(metricsTimer, false, "BUSINESS_RULE_ERROR");
                                
                                return ResolveExceptionResult.failure(error, operationId, performedBy);

                        } catch (IllegalStateException e) {
                                log.error("Resolution state error for transaction: {}, error: {}, operation: {}",
                                                input.getTransactionId(), e.getMessage(), operationId);

                                GraphQLError error = GraphQLErrorHandler.createBusinessRuleError(
                                        MutationErrorCode.CONCURRENT_MODIFICATION,
                                        e.getMessage()
                                );
                                
                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);
                                
                                mutationMetrics.recordResolveOperation(metricsTimer, false, "BUSINESS_RULE_ERROR");
                                
                                return ResolveExceptionResult.failure(error, operationId, performedBy);

                        } catch (SecurityException e) {
                                log.warn("Resolution security error for transaction: {}, error: {}, operation: {}",
                                                input.getTransactionId(), e.getMessage(), operationId);

                                GraphQLError error = GraphQLErrorHandler.createSecurityError(
                                        MutationErrorCode.INSUFFICIENT_PERMISSIONS,
                                        e.getMessage()
                                );
                                
                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);
                                
                                mutationMetrics.recordResolveOperation(metricsTimer, false, "SECURITY_ERROR");
                                
                                return ResolveExceptionResult.failure(error, operationId, performedBy);

                        } catch (Exception e) {
                                log.error("GraphQL resolve exception failed for transaction: {}, error: {}, operation: {}",
                                                input.getTransactionId(), e.getMessage(), operationId, e);

                                GraphQLError error = GraphQLErrorHandler.createSystemError(
                                        MutationErrorCode.DATABASE_ERROR,
                                        "Resolution operation failed: " + e.getMessage()
                                );
                                
                                long executionTime = System.currentTimeMillis() - startTime;
                                auditLogger.logMutationResult(operationId, false, List.of(error), executionTime);
                                
                                mutationMetrics.recordResolveOperation(metricsTimer, false, "SYSTEM_ERROR");
                                
                                return ResolveExceptionResult.failure(error, operationId, performedBy);
                        }
                });
        }

        /**
         * Builds simplified acknowledgment notes from the input.
         * Combines reason and notes only - simplified from previous version.
         *
         * @param input the acknowledgment input
         * @return formatted acknowledgment notes
         */
        private String buildAcknowledgmentNotes(AcknowledgeExceptionInput input) {
                StringBuilder notes = new StringBuilder();
                notes.append("Reason: ").append(input.getReason());

                if (input.getNotes() != null && !input.getNotes().trim().isEmpty()) {
                        notes.append("\nNotes: ").append(input.getNotes());
                }

                return notes.toString();
        }


}