package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.api.dto.ErrorResponse;
import com.arcone.biopro.exception.collector.api.dto.RetryAttemptResponse;
import com.arcone.biopro.exception.collector.api.dto.RetryRequest;
import com.arcone.biopro.exception.collector.api.dto.RetryResponse;
import com.arcone.biopro.exception.collector.application.service.RetryService;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing retry operations on interface exceptions.
 * Provides endpoints for initiating retries, viewing retry history, and
 * managing retry operations.
 */
@RestController
@RequestMapping("/api/v1/exceptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Retry Management", description = "Operations for managing exception retry functionality")
public class RetryController {

    private final RetryService retryService;

    /**
     * Initiates a retry operation for the specified exception.
     *
     * @param transactionId the transaction ID of the exception to retry
     * @param retryRequest  the retry request details
     * @return RetryResponse containing retry operation details
     */
    @PostMapping("/{transactionId}/retry")
    @Operation(summary = "Initiate retry operation", 
               description = "Initiates a retry operation for a failed interface request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Retry operation initiated successfully",
                    content = @Content(schema = @Schema(implementation = RetryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Exception not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Exception is not retryable or retry already in progress",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> initiateRetry(
            @Parameter(description = "Transaction ID of the exception to retry", required = true)
            @PathVariable String transactionId,
            @Valid @RequestBody RetryRequest retryRequest) {
        
        log.info("Received retry request for transaction: {}, initiated by: {}", 
                transactionId, retryRequest.getInitiatedBy());

        try {
            // Validate if retry is possible
            if (!retryService.canRetry(transactionId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ErrorResponse.builder()
                      .error("RETRY_NOT_ALLOWED")
                                .message("Exception is not retryable or retry already in progress")
                                .path("/api/v1/exceptions/" + transactionId + "/retry")
                                .build());
            }

            RetryResponse response = retryService.initiateRetry(transactionId, retryRequest);
            
            log.info("Retry initiated successfully for transaction: {}, retry ID: {}", 
                    transactionId, response.getRetryId());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid retry request for transaction: {}, error: {}", transactionId, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .error("EXCEPTION_NOT_FOUND")
                            .message(e.getMessage())
                            .path("/api/v1/exceptions/" + transactionId + "/retry")
                            .build());

        } catch (Exception e) {
            log.error("Failed to initiate retry for transaction: {}, error: {}", 
                    transactionId, e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("RETRY_INITIATION_FAILED")
                            .message("Failed to initiate retry operation")
                            .path("/api/v1/exceptions/" + transactionId + "/retry")
                            .build());
        }
    }

    /**
     * Retrieves the complete retry history for an exception.
     *
     * @param transactionId the transaction ID
     * @return List of retry attempts
     */
    @GetMapping("/{transactionId}/retry-history")
    @Operation(summary = "Get retry history", 
               description = "Retrieves the complete retry history for an exception")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retry history retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Exception not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getRetryHistory(
            @Parameter(description = "Transaction ID of the exception", required = true)
            @PathVariable String transactionId) {
        
        log.debug("Retrieving retry history for transaction: {}", transactionId);

        try {
            List<RetryAttempt> retryHistory = retryService.getRetryHistory(transactionId);
            
            List<RetryAttemptResponse> response = retryHistory.stream()
                    .map(this::mapToRetryAttemptResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Exception not found for retry history request: {}", transactionId);
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .error("EXCEPTION_NOT_FOUND")
                            .message(e.getMessage())
                            .path("/api/v1/exceptions/" + transactionId + "/retry-history")
                            .build());

        } catch (Exception e) {
            log.error("Failed to retrieve retry history for transaction: {}, error: {}", 
                    transactionId, e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("RETRY_HISTORY_RETRIEVAL_FAILED")
                            .message("Failed to retrieve retry history")
                            .path("/api/v1/exceptions/" + transactionId + "/retry-history")
                            .build());
        }
    }

    /**
     * Gets the latest retry attempt for an exception.
     *
     * @param transactionId the transaction ID
     * @return Latest retry attempt or 404 if none exists
     */
    @GetMapping("/{transactionId}/retry/latest")
    @Operation(summary = "Get latest retry attempt", 
               description = "Retrieves the most recent retry attempt for an exception")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Latest retry attempt retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Exception not found or no retry attempts exist",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getLatestRetryAttempt(
            @Parameter(description = "Transaction ID of the exception", required = true)
            @PathVariable String transactionId) {
        
        log.debug("Retrieving latest retry attempt for transaction: {}", transactionId);

        try {
            Optional<RetryAttempt> latestAttempt = retryService.getLatestRetryAttempt(transactionId);
            
            if (latestAttempt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.builder()
                                .error("NO_RETRY_ATTEMPTS")
                                .message("No retry attempts found for this exception")
                                .path("/api/v1/exceptions/" + transactionId + "/retry/latest")
                                .build());
            }

            RetryAttemptResponse response = mapToRetryAttemptResponse(latestAttempt.get());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Exception not found for latest retry request: {}", transactionId);
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .error("EXCEPTION_NOT_FOUND")
                            .message(e.getMessage())
                            .path("/api/v1/exceptions/" + transactionId + "/retry/latest")
                            .build());

        } catch (Exception e) {
            log.error("Failed to retrieve latest retry attempt for transaction: {}, error: {}", 
                    transactionId, e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("LATEST_RETRY_RETRIEVAL_FAILED")
                            .message("Failed to retrieve latest retry attempt")
                            .path("/api/v1/exceptions/" + transactionId + "/retry/latest")
                            .build());
        }
    }

    /**
     * Gets retry statistics for an exception.
     *
     * @param transactionId the transaction ID
     * @return Retry statistics
     */
    @GetMapping("/{transactionId}/retry/statistics")
    @Operation(summary = "Get retry statistics", 
               description = "Retrieves retry statistics for an exception")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retry statistics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Exception not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getRetryStatistics(
            @Parameter(description = "Transaction ID of the exception", required = true)
            @PathVariable String transactionId) {
        
        log.debug("Retrieving retry statistics for transaction: {}", transactionId);

        try {
            Object[] stats = retryService.getRetryStatistics(transactionId);
            
            return ResponseEntity.ok(new RetryStatisticsResponse(
                    ((Number) stats[0]).longValue(),  // totalAttempts
                    ((Number) stats[1]).longValue(),  // successfulAttempts
                    ((Number) stats[2]).longValue(),  // failedAttempts
                    ((Number) stats[3]).longValue()   // pendingAttempts
            ));

        } catch (IllegalArgumentException e) {
            log.warn("Exception not found for retry statistics request: {}", transactionId);
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .error("EXCEPTION_NOT_FOUND")
                            .message(e.getMessage())
                            .path("/api/v1/exceptions/" + transactionId + "/retry/statistics")
                            .build());

        } catch (Exception e) {
            log.error("Failed to retrieve retry statistics for transaction: {}, error: {}", 
                    transactionId, e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("RETRY_STATISTICS_RETRIEVAL_FAILED")
                            .message("Failed to retrieve retry statistics")
                            .path("/api/v1/exceptions/" + transactionId + "/retry/statistics")
                            .build());
        }
    }

    /**
     * Cancels a pending retry attempt.
     *
     * @param transactionId the transaction ID
     * @param attemptNumber the attempt number to cancel
     * @return Success or error response
     */
    @DeleteMapping("/{transactionId}/retry/{attemptNumber}")
    @Operation(summary = "Cancel retry attempt", 
               description = "Cancels a pending retry attempt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retry attempt cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Exception or retry attempt not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Retry attempt cannot be cancelled",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> cancelRetry(
            @Parameter(description = "Transaction ID of the exception", required = true)
            @PathVariable String transactionId,
            @Parameter(description = "Attempt number to cancel", required = true)
            @PathVariable Integer attemptNumber) {
        
        log.info("Cancelling retry attempt for transaction: {}, attempt: {}", transactionId, attemptNumber);

        try {
            boolean cancelled = retryService.cancelRetry(transactionId, attemptNumber);
            
            if (!cancelled) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ErrorResponse.builder()
                                .error("RETRY_CANCELLATION_FAILED")
                                .message("Retry attempt cannot be cancelled or does not exist")
                                .path("/api/v1/exceptions/" + transactionId + "/retry/" + attemptNumber)
                                .build());
            }

            return ResponseEntity.ok().body(new CancelRetryResponse(
                    "Retry attempt cancelled successfully",
                    transactionId,
                    attemptNumber
            ));

        } catch (IllegalArgumentException e) {
            log.warn("Exception not found for retry cancellation: {}", transactionId);
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .error("EXCEPTION_NOT_FOUND")
                            .message(e.getMessage())
                            .path("/api/v1/exceptions/" + transactionId + "/retry/" + attemptNumber)
                            .build());

        } catch (Exception e) {
            log.error("Failed to cancel retry for transaction: {}, attempt: {}, error: {}", 
                    transactionId, attemptNumber, e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("RETRY_CANCELLATION_ERROR")
                            .message("Failed to cancel retry attempt")
                            .path("/api/v1/exceptions/" + transactionId + "/retry/" + attemptNumber)
                            .build());
        }
    }

    /**
     * Maps RetryAttempt entity to RetryAttemptResponse DTO.
     */
    private RetryAttemptResponse mapToRetryAttemptResponse(RetryAttempt retryAttempt) {
        return RetryAttemptResponse.builder()
                .id(retryAttempt.getId())
                .attemptNumber(retryAttempt.getAttemptNumber())
                .status(retryAttempt.getStatus().name())
                .initiatedBy(retryAttempt.getInitiatedBy())
                .initiatedAt(retryAttempt.getInitiatedAt())
                .completedAt(retryAttempt.getCompletedAt())
                .resultSuccess(retryAttempt.getResultSuccess())
                .resultMessage(retryAttempt.getResultMessage())
                .resultResponseCode(retryAttempt.getResultResponseCode())
                .resultErrorDetails(retryAttempt.getResultErrorDetails())
                .build();
    }

    /**
     * Response DTO for retry statistics.
     */
    public record RetryStatisticsResponse(
            long totalAttempts,
            long successfulAttempts,
            long failedAttempts,
            long pendingAttempts
    ) {}

    /**
     * Response DTO for retry cancellation.
     */
    public record CancelRetryResponse(
            String message,
            String transactionId,
            Integer attemptNumber
    ) {}
}