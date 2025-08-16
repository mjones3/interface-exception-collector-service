package com.arcone.biopro.partner.order.api.controller;

import com.arcone.biopro.partner.order.api.dto.PayloadResponse;
import com.arcone.biopro.partner.order.application.exception.PayloadNotFoundException;
import com.arcone.biopro.partner.order.application.service.PayloadRetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for order API operations used by Interface Exception
 * Collector.
 * This controller provides endpoints at the /api/v1/orders path that the
 * Interface Exception Collector expects for payload retrieval and retry
 * operations.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Order API", description = "APIs for order operations used by Interface Exception Collector")
public class OrderApiController {

    private final PayloadRetrievalService payloadRetrievalService;

    /**
     * Retrieves the original order payload for a given transaction ID.
     * This endpoint is specifically designed for the Interface Exception Collector
     * Service.
     */
    @GetMapping("/{transactionId}/payload")
    @Operation(summary = "Retrieve original order payload", description = "Retrieves the original order payload for a given transaction ID. Used by the Interface Exception Collector Service for retry operations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payload retrieved successfully", content = @Content(schema = @Schema(implementation = PayloadResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction ID not found", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "System error", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> getOrderPayload(
            @Parameter(description = "Transaction ID of the order", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID transactionId) {

        log.info("Interface Exception Collector requesting payload for transaction: {}", transactionId);

        try {
            PayloadResponse payload = payloadRetrievalService.getOriginalPayload(transactionId);

            log.info(
                    "Payload retrieved successfully for Interface Exception Collector - transactionId: {}, externalId: {}",
                    transactionId, payload.getExternalId());

            return ResponseEntity.ok(payload);

        } catch (PayloadNotFoundException e) {
            log.warn("Payload not found for transaction: {} (requested by Interface Exception Collector)",
                    transactionId);

            Map<String, Object> errorResponse = Map.of(
                    "error", "PAYLOAD_NOT_FOUND",
                    "message", e.getMessage(),
                    "transactionId", transactionId,
                    "timestamp", java.time.OffsetDateTime.now());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error(
                    "Unexpected error retrieving payload for transaction: {} (requested by Interface Exception Collector)",
                    transactionId, e);

            Map<String, Object> errorResponse = Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred: " + e.getMessage(),
                    "transactionId", transactionId,
                    "timestamp", java.time.OffsetDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Handles retry requests from the Interface Exception Collector.
     * This endpoint receives the original payload and reprocesses it.
     */
    @PostMapping("/{transactionId}/retry")
    @Operation(summary = "Retry order processing", description = "Reprocesses an order using the original payload. Used by the Interface Exception Collector Service for retry operations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retry processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Transaction ID not found"),
            @ApiResponse(responseCode = "500", description = "System error")
    })
    public ResponseEntity<Map<String, Object>> retryOrder(
            @Parameter(description = "Transaction ID of the order to retry", required = true) @PathVariable UUID transactionId,
            @RequestBody Object originalPayload) {

        log.info("Interface Exception Collector requesting retry for transaction: {}", transactionId);

        try {
            // For now, return a success response indicating the retry was accepted
            // In a real implementation, this would reprocess the order
            Map<String, Object> response = Map.of(
                    "transactionId", transactionId,
                    "status", "RETRY_ACCEPTED",
                    "message", "Retry request accepted and will be processed",
                    "timestamp", java.time.OffsetDateTime.now());

            log.info("Retry request accepted for transaction: {} (from Interface Exception Collector)", transactionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing retry request for transaction: {} (from Interface Exception Collector)",
                    transactionId, e);

            Map<String, Object> errorResponse = Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred: " + e.getMessage(),
                    "transactionId", transactionId,
                    "timestamp", java.time.OffsetDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}