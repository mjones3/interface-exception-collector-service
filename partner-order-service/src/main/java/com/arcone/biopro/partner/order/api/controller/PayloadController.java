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
 * REST controller for payload retrieval operations.
 * Used by the Interface Exception Collector Service to retrieve original
 * payloads for retry operations.
 */
@RestController
@RequestMapping("/v1/partner-order-provider")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Payload Retrieval", description = "APIs for retrieving original order payloads for retry operations")
public class PayloadController {

    private final PayloadRetrievalService payloadRetrievalService;

    /**
     * Retrieves the original order payload for a given transaction ID.
     * This endpoint is used by the Interface Exception Collector Service to
     * retrieve payloads for retry operations.
     */
    @GetMapping("/orders/{transactionId}/payload")
    @Operation(summary = "Retrieve original order payload", description = "Retrieves the original order payload for a given transaction ID. Used by the Interface Exception Collector Service to retrieve payloads for retry operations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payload retrieved successfully", content = @Content(schema = @Schema(implementation = PayloadResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction ID not found", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "System error", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> getPayload(
            @Parameter(description = "Transaction ID of the order", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID transactionId) {

        log.info("Received payload retrieval request for transaction: {}", transactionId);

        try {
            PayloadResponse payload = payloadRetrievalService.getOriginalPayload(transactionId);

            log.info("Payload retrieved successfully - transactionId: {}, externalId: {}, payloadSize: {}",
                    transactionId, payload.getExternalId(), payload.getPayloadSize());

            return ResponseEntity.ok(payload);

        } catch (PayloadNotFoundException e) {
            log.warn("Payload not found for transaction: {}", transactionId);

            Map<String, Object> errorResponse = Map.of(
                    "error", "PAYLOAD_NOT_FOUND",
                    "message", e.getMessage(),
                    "transactionId", transactionId,
                    "timestamp", java.time.OffsetDateTime.now());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error retrieving payload for transaction: {}", transactionId, e);

            Map<String, Object> errorResponse = Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred: " + e.getMessage(),
                    "transactionId", transactionId,
                    "timestamp", java.time.OffsetDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Checks if a payload exists for the given transaction ID.
     * Lightweight endpoint for existence checks.
     */
    @GetMapping("/orders/{transactionId}/payload/exists")
    @Operation(summary = "Check if payload exists", description = "Checks if a payload exists for the given transaction ID without retrieving the full payload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Existence check completed", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "System error", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> checkPayloadExists(
            @Parameter(description = "Transaction ID of the order", required = true) @PathVariable UUID transactionId) {

        log.debug("Checking payload existence for transaction: {}", transactionId);

        try {
            boolean exists = payloadRetrievalService.payloadExists(transactionId);

            Map<String, Object> response = Map.of(
                    "transactionId", transactionId,
                    "exists", exists,
                    "timestamp", java.time.OffsetDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking payload existence for transaction: {}", transactionId, e);

            Map<String, Object> errorResponse = Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "An unexpected error occurred: " + e.getMessage(),
                    "transactionId", transactionId,
                    "exists", false,
                    "timestamp", java.time.OffsetDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}