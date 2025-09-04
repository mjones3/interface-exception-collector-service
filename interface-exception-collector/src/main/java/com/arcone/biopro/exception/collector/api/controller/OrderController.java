package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.client.MockRSocketOrderServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * REST controller for order data retrieval using Mock RSocket Server.
 * This controller provides endpoints to test the RSocket integration
 * and retrieve order data from the mock server.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Data", description = "APIs for retrieving order data from mock RSocket server")
@ConditionalOnProperty(name = "app.rsocket.mock-server.enabled", havingValue = "true")
public class OrderController {

    private final MockRSocketOrderServiceClient mockRSocketClient;

    /**
     * Retrieves order data by external ID from the mock RSocket server.
     * This endpoint demonstrates the RSocket integration functionality.
     */
    @GetMapping("/{externalId}")
    @Operation(summary = "Get order data by external ID", 
               description = "Retrieves order data from the mock RSocket server using the provided external ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved order data"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Failed to retrieve order data")
    })
    public CompletableFuture<ResponseEntity<Object>> getOrderData(
            @Parameter(description = "External order identifier", required = true) 
            @PathVariable String externalId) {
        
        log.info("Retrieving order data for external ID: {}", externalId);
        
        // Create a minimal InterfaceException for the request
        InterfaceException exception = InterfaceException.builder()
            .externalId(externalId)
            .transactionId("order-request-" + System.currentTimeMillis())
            .interfaceType(InterfaceType.ORDER)
            .build();
        
        return mockRSocketClient.getOriginalPayload(exception)
            .thenApply(orderData -> {
                if (orderData != null) {
                    log.info("Successfully retrieved order data for external ID: {}", externalId);
                    return ResponseEntity.ok((Object) orderData);
                } else {
                    log.warn("Order not found for external ID: {}", externalId);
                    return ResponseEntity.notFound().<Object>build();
                }
            })
            .exceptionally(throwable -> {
                log.error("Failed to retrieve order data for external ID: {}", externalId, throwable);
                return ResponseEntity.internalServerError().<Object>build();
            });
    }

    /**
     * Health check endpoint for the mock RSocket server connection.
     */
    @GetMapping("/health")
    @Operation(summary = "Check mock RSocket server health", 
               description = "Performs a health check on the mock RSocket server connection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mock server is healthy"),
        @ApiResponse(responseCode = "503", description = "Mock server is unavailable")
    })
    public CompletableFuture<ResponseEntity<Object>> checkHealth() {
        log.info("Performing health check on mock RSocket server");
        
        // Create a health check exception
        InterfaceException healthCheck = InterfaceException.builder()
            .externalId("HEALTH-CHECK")
            .transactionId("health-" + System.currentTimeMillis())
            .interfaceType(InterfaceType.ORDER)
            .build();
        
        return mockRSocketClient.getOriginalPayload(healthCheck)
            .thenApply(result -> {
                log.info("Mock RSocket server health check successful");
                return ResponseEntity.ok((Object) java.util.Map.of(
                    "status", "UP",
                    "service", "mock-rsocket-server",
                    "timestamp", java.time.OffsetDateTime.now()
                ));
            })
            .exceptionally(throwable -> {
                log.error("Mock RSocket server health check failed", throwable);
                return ResponseEntity.status(503).body((Object) java.util.Map.of(
                    "status", "DOWN",
                    "service", "mock-rsocket-server",
                    "error", throwable.getMessage(),
                    "timestamp", java.time.OffsetDateTime.now()
                ));
            });
    }
}