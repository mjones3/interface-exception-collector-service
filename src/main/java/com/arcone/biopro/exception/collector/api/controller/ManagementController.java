package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.api.dto.AcknowledgeRequest;
import com.arcone.biopro.exception.collector.api.dto.AcknowledgeResponse;
import com.arcone.biopro.exception.collector.api.dto.ErrorResponse;
import com.arcone.biopro.exception.collector.api.dto.ResolveRequest;
import com.arcone.biopro.exception.collector.api.dto.ResolveResponse;
import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for exception lifecycle management operations.
 * Provides endpoints for acknowledging and resolving exceptions.
 * Implements requirements US-013 and US-014 for exception acknowledgment and
 * resolution.
 */
@RestController
@RequestMapping("/api/v1/exceptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Exception Lifecycle Management", description = "Operations for managing exception acknowledgment and resolution")
public class ManagementController {

    private final ExceptionManagementService managementService;

    /**
     * Acknowledges an exception by updating its status and audit information.
     * Implements requirement US-013 for exception acknowledgment.
     *
     * @param transactionId the transaction ID of the exception to acknowledge
     * @param request       the acknowledgment request containing user and notes
     * @return AcknowledgeResponse with acknowledgment details
     */
    @PutMapping("/{transactionId}/acknowledge")
    @Operation(summary = "Acknowledge exception", description = "Acknowledges an exception by updating its status to ACKNOWLEDGED and recording audit information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exception acknowledged successfully", content = @Content(schema = @Schema(implementation = AcknowledgeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exception not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Exception cannot be acknowledged (already resolved or closed)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> acknowledgeException(
            @Parameter(description = "Transaction ID of the exception to acknowledge", required = true) @PathVariable String transactionId,
            @Valid @RequestBody AcknowledgeRequest request) {

        log.info("Received acknowledgment request for transaction: {}, acknowledged by: {}",
                transactionId, request.getAcknowledgedBy());

        try {
            // Check if the exception can be acknowledged
            if (!managementService.canAcknowledge(transactionId)) {
                log.warn("Exception cannot be acknowledged - transaction: {}", transactionId);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ErrorResponse.builder()
                                .error("ACKNOWLEDGMENT_NOT_ALLOWED")
                                .message("Exception cannot be acknowledged (not found, already resolved, or closed)")
                                .path("/api/v1/exceptions/" + transactionId + "/acknowledge")
                                .build());
            }

            // Acknowledge the exception
            AcknowledgeResponse response = managementService.acknowledgeException(transactionId, request);

            log.info("Exception acknowledged successfully - transaction: {}, acknowledged by: {}, at: {}",
                    transactionId, response.getAcknowledgedBy(), response.getAcknowledgedAt());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Exception not found for acknowledgment - transaction: {}, error: {}",
                    transactionId, e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .error("EXCEPTION_NOT_FOUND")
                            .message(e.getMessage())
                            .path("/api/v1/exceptions/" + transactionId + "/acknowledge")
                            .build());

        } catch (Exception e) {
            log.error("Failed to acknowledge exception - transaction: {}, error: {}",
                    transactionId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("ACKNOWLEDGMENT_FAILED")
                            .message("Failed to acknowledge exception")
                            .path("/api/v1/exceptions/" + transactionId + "/acknowledge")
                            .build());
        }
    }

    /**
     * Resolves an exception by updating its status and resolution information.
     * Publishes an ExceptionResolved event upon successful resolution.
     * Implements requirement US-014 for exception resolution management.
     *
     * @param transactionId the transaction ID of the exception to resolve
     * @param request       the resolution request containing resolution details
     * @return ResolveResponse with resolution details
     */
    @PutMapping("/{transactionId}/resolve")
    @Operation(summary = "Resolve exception", description = "Resolves an exception by updating its status to RESOLVED and recording resolution information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exception resolved successfully", content = @Content(schema = @Schema(implementation = ResolveResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exception not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Exception cannot be resolved (already resolved or closed)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> resolveException(
            @Parameter(description = "Transaction ID of the exception to resolve", required = true) @PathVariable String transactionId,
            @Valid @RequestBody ResolveRequest request) {

        log.info("Received resolution request for transaction: {}, resolved by: {}, method: {}",
                transactionId, request.getResolvedBy(), request.getResolutionMethod());

        try {
            // Check if the exception can be resolved
            if (!managementService.canResolve(transactionId)) {
                log.warn("Exception cannot be resolved - transaction: {}", transactionId);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ErrorResponse.builder()
                                .error("RESOLUTION_NOT_ALLOWED")
                                .message("Exception cannot be resolved (not found, already resolved, or closed)")
                                .path("/api/v1/exceptions/" + transactionId + "/resolve")
                                .build());
            }

            // Resolve the exception
            ResolveResponse response = managementService.resolveException(transactionId, request);

            log.info("Exception resolved successfully - transaction: {}, resolved by: {}, method: {}, at: {}",
                    transactionId, response.getResolvedBy(), response.getResolutionMethod(),
                    response.getResolvedAt());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Exception not found for resolution - transaction: {}, error: {}",
                    transactionId, e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder()
                            .error("EXCEPTION_NOT_FOUND")
                            .message(e.getMessage())
                            .path("/api/v1/exceptions/" + transactionId + "/resolve")
                            .build());

        } catch (Exception e) {
            log.error("Failed to resolve exception - transaction: {}, error: {}",
                    transactionId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("RESOLUTION_FAILED")
                            .message("Failed to resolve exception")
                            .path("/api/v1/exceptions/" + transactionId + "/resolve")
                            .build());
        }
    }
}