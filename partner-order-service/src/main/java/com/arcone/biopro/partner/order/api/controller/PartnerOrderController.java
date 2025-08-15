package com.arcone.biopro.partner.order.api.controller;

import com.arcone.biopro.partner.order.api.dto.PartnerOrderRequest;
import com.arcone.biopro.partner.order.api.dto.PartnerOrderResponse;
import com.arcone.biopro.partner.order.application.exception.DuplicateExternalIdException;
import com.arcone.biopro.partner.order.application.service.PartnerOrderService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for partner order submissions.
 * Handles both new orders and retry requests from Interface Exception
 * Collector.
 */
@RestController
@RequestMapping("/v1/partner-order-provider")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Partner Order Management", description = "APIs for partner order submissions and retry operations")
public class PartnerOrderController {

    private final PartnerOrderService partnerOrderService;

    /**
     * Creates a new order from partner submission or processes a retry request.
     * Supports both new order submissions and retry requests from Interface
     * Exception Collector.
     */
    @PostMapping("/orders")
    @Operation(summary = "Submit partner order", description = "Creates a new order from partner submission or processes a retry request initiated by the Interface Exception Collector Service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Order accepted for processing", content = @Content(schema = @Schema(implementation = PartnerOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation errors", content = @Content(schema = @Schema(implementation = PartnerOrderResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate external ID", content = @Content(schema = @Schema(implementation = PartnerOrderResponse.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
            @ApiResponse(responseCode = "500", description = "System error", content = @Content(schema = @Schema(implementation = PartnerOrderResponse.class)))
    })
    public ResponseEntity<PartnerOrderResponse> submitOrder(
            @Parameter(description = "Partner order details", required = true) @Valid @RequestBody PartnerOrderRequest request,

            @Parameter(description = "Retry attempt number (set by Interface Exception Collector)") @RequestHeader(value = "X-Retry-Attempt", required = false) Integer retryAttempt,

            @Parameter(description = "Original transaction ID for retry correlation (set by Interface Exception Collector)") @RequestHeader(value = "X-Original-Transaction-ID", required = false) String originalTransactionIdHeader) {

        log.info("Received partner order submission - externalId: {}, retryAttempt: {}, originalTransactionId: {}",
                request.getExternalId(), retryAttempt, originalTransactionIdHeader);

        try {
            boolean isRetry = retryAttempt != null && retryAttempt > 0;
            UUID originalTransactionId = null;

            if (isRetry && originalTransactionIdHeader != null) {
                try {
                    originalTransactionId = UUID.fromString(originalTransactionIdHeader);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid original transaction ID format: {}", originalTransactionIdHeader);
                }
            }

            PartnerOrderResponse response;
            if (isRetry) {
                log.info("Processing retry request - externalId: {}, attempt: {}",
                        request.getExternalId(), retryAttempt);
                response = partnerOrderService.processRetry(request, originalTransactionId);
            } else {
                log.info("Processing new order submission - externalId: {}", request.getExternalId());
                response = partnerOrderService.processOrder(request);
            }

            // Determine HTTP status based on response status
            HttpStatus httpStatus = determineHttpStatus(response.getStatus());

            log.info("Partner order submission completed - externalId: {}, status: {}, transactionId: {}",
                    request.getExternalId(), response.getStatus(), response.getTransactionId());

            return ResponseEntity.status(httpStatus).body(response);

        } catch (DuplicateExternalIdException e) {
            log.warn("Duplicate external ID: {}", e.getExternalId());
            PartnerOrderResponse errorResponse = PartnerOrderResponse.duplicateError(
                    e.getExternalId(), UUID.randomUUID());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error processing partner order - externalId: {}",
                    request.getExternalId(), e);
            PartnerOrderResponse errorResponse = PartnerOrderResponse.error(
                    "INTERNAL_ERROR",
                    "An unexpected error occurred: " + e.getMessage(),
                    request.getExternalId(),
                    UUID.randomUUID());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Determines the appropriate HTTP status code based on the response status.
     */
    private HttpStatus determineHttpStatus(String status) {
        return switch (status) {
            case "ACCEPTED" -> HttpStatus.ACCEPTED;
            case "VALIDATION_FAILED" -> HttpStatus.BAD_REQUEST;
            case "DUPLICATE_EXTERNAL_ID" -> HttpStatus.CONFLICT;
            case "PROCESSING_FAILED", "INTERNAL_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.ACCEPTED;
        };
    }
}