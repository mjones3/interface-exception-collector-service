package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Hybrid client for Partner Order Service that uses:
 * - RSocket for retrieving order data after OrderRejected events
 * - REST for submitting retry requests to the partner order service
 * 
 * This implements the architecture where:
 * 1. Order data retrieval uses RSocket communication
 * 2. Order retry submissions use REST POST to /v1/partner-order-provider/orders
 */
@Component
@Slf4j
public class PartnerOrderServiceClient extends BaseSourceServiceClient {

    @Value("${source-services.partner-order.api-key:#{null}}")
    private String apiKey;

    @Value("${source-services.partner-order.auth-header:X-API-Key}")
    private String authHeader;

    @Value("${source-services.partner-order.rsocket.host:localhost}")
    private String rsocketHost;

    @Value("${source-services.partner-order.rsocket.port:7000}")
    private int rsocketPort;

    @Value("${source-services.partner-order.rsocket.timeout:PT10S}")
    private Duration rsocketTimeout;

    @Value("${source-services.partner-order.rest.retry-url:http://localhost:8090/v1/partner-order-provider/orders}")
    private String retryUrl;

    private final ObjectMapper objectMapper;
    private final RSocketRequester.Builder rSocketRequesterBuilder;
    private RSocketRequester rSocketRequester;

    @Autowired(required = false)
    private RSocketConnectionManager connectionManager;

    public PartnerOrderServiceClient(RestTemplate restTemplate,
            @Value("${source-services.partner-order.base-url}") String baseUrl,
            ObjectMapper objectMapper,
            RSocketRequester.Builder rSocketRequesterBuilder) {
        super(restTemplate, baseUrl, "partner-order-service");
        this.objectMapper = objectMapper;
        this.rSocketRequesterBuilder = rSocketRequesterBuilder;
        log.info("Initializing PartnerOrderServiceClient with base URL: {} and RSocket {}:{}", 
                baseUrl, rsocketHost, rsocketPort);
        log.info("PartnerOrderServiceClient configured for hybrid RSocket retrieval + REST retry");
    }

    @Override
    public boolean supports(String interfaceType) {
        boolean supports = InterfaceType.PARTNER_ORDER.name().equals(interfaceType) || 
                          InterfaceType.ORDER.name().equals(interfaceType);
        log.debug("PartnerOrderServiceClient supports interface type '{}': {}", interfaceType, supports);
        return supports;
    }

    /**
     * Retrieves order data using RSocket communication.
     * This is called after OrderRejected events to get the original order payload.
     */
    @Override
    @CircuitBreaker(name = "partner-order-rsocket", fallbackMethod = "fallbackGetPayload")
    @TimeLimiter(name = "partner-order-rsocket")
    @Retry(name = "partner-order-rsocket")
    public CompletableFuture<PayloadResponse> getOriginalPayload(InterfaceException exception) {
        log.info("Retrieving order data via RSocket for externalId: {}, transactionId: {}", 
                exception.getExternalId(), exception.getTransactionId());

        return CompletableFuture.supplyAsync(() -> {
            try {
                RSocketRequester requester = getRSocketRequester();
                if (requester == null) {
                    throw new RuntimeException("RSocket connection not available");
                }

                String route = "orders." + exception.getExternalId();
                log.debug("Making RSocket call with route: {}", route);

                String orderData = requester
                    .route(route)
                    .retrieveMono(String.class)
                    .timeout(rsocketTimeout)
                    .doOnError(error -> log.error("RSocket call failed for externalId: {}, error: {}", 
                            exception.getExternalId(), error.getMessage()))
                    .doOnSuccess(data -> log.debug("Successfully retrieved order data for externalId: {}", 
                            exception.getExternalId()))
                    .block();

                boolean dataRetrieved = orderData != null;
                log.info("RSocket order data retrieval completed for externalId: {}, retrieved: {}", 
                        exception.getExternalId(), dataRetrieved);

                return PayloadResponse.builder()
                        .transactionId(exception.getTransactionId())
                        .interfaceType(exception.getInterfaceType().name())
                        .payload(orderData)
                        .sourceService("partner-order-service-rsocket")
                        .retrieved(dataRetrieved)
                        .build();

            } catch (Exception e) {
                log.error("Failed to retrieve order data via RSocket for externalId: {}, error: {}", 
                        exception.getExternalId(), e.getMessage(), e);

                return PayloadResponse.builder()
                        .transactionId(exception.getTransactionId())
                        .interfaceType(exception.getInterfaceType().name())
                        .sourceService("partner-order-service-rsocket")
                        .retrieved(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        });
    }

    /**
     * Submits retry requests using REST POST to the partner order service.
     * Transforms the stored order data into the expected PartnerOrderRequest format.
     */
    @Override
    @CircuitBreaker(name = "partner-order-rest", fallbackMethod = "fallbackSubmitRetry")
    @TimeLimiter(name = "partner-order-rest")
    @Retry(name = "partner-order-rest")
    public CompletableFuture<ResponseEntity<Object>> submitRetry(InterfaceException exception, Object payload) {
        log.info("Submitting retry via REST for externalId: {}, transactionId: {}", 
                exception.getExternalId(), exception.getTransactionId());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Transform the payload to PartnerOrderRequest format
                Object transformedPayload = transformToPartnerOrderRequest(payload, exception);
                
                log.debug("Submitting retry to: {}", retryUrl);

                HttpHeaders headers = createRetryHeaders(exception);
                // Add retry-specific headers for partner order service
                headers.set("X-Retry-Attempt", String.valueOf(exception.getRetryCount() + 1));
                headers.set("X-Original-Transaction-ID", exception.getTransactionId());

                HttpEntity<Object> requestEntity = new HttpEntity<>(transformedPayload, headers);

                ResponseEntity<Object> response = restTemplate.exchange(
                        retryUrl,
                        HttpMethod.POST,
                        requestEntity,
                        Object.class);

                log.info("Successfully submitted retry via REST for externalId: {}, status: {}", 
                        exception.getExternalId(), response.getStatusCode());

                return response;

            } catch (Exception e) {
                log.error("Failed to submit retry via REST for externalId: {}, error: {}", 
                        exception.getExternalId(), e.getMessage(), e);
                throw new RuntimeException("REST retry submission failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Transforms the stored order data into PartnerOrderRequest format for REST retry.
     */
    private Object transformToPartnerOrderRequest(Object payload, InterfaceException exception) {
        try {
            // If payload is already a string (JSON), parse it
            JsonNode orderData;
            if (payload instanceof String) {
                orderData = objectMapper.readTree((String) payload);
            } else {
                orderData = objectMapper.valueToTree(payload);
            }

            // Create PartnerOrderRequest structure
            JsonNode partnerOrderRequest = objectMapper.createObjectNode()
                .put("externalId", exception.getExternalId())
                .put("orderStatus", "OPEN")
                .put("locationCode", extractLocationCode(orderData, exception))
                .put("shipmentType", "CUSTOMER")
                .put("productCategory", extractProductCategory(orderData))
                .set("orderItems", transformOrderItems(orderData));

            // Add optional fields if available
            if (orderData.has("createDate")) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) partnerOrderRequest)
                    .put("createDate", orderData.get("createDate").asText());
            }
            if (orderData.has("deliveryType")) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) partnerOrderRequest)
                    .put("deliveryType", orderData.get("deliveryType").asText());
            }

            log.debug("Transformed order data to PartnerOrderRequest format for externalId: {}", 
                    exception.getExternalId());

            return partnerOrderRequest;

        } catch (Exception e) {
            log.error("Failed to transform order data for externalId: {}, error: {}", 
                    exception.getExternalId(), e.getMessage());
            throw new RuntimeException("Order data transformation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts location code from order data or uses exception data as fallback.
     */
    private String extractLocationCode(JsonNode orderData, InterfaceException exception) {
        if (orderData.has("locationCode")) {
            return orderData.get("locationCode").asText();
        }
        if (exception.getLocationCode() != null) {
            return exception.getLocationCode();
        }
        return "DEFAULT_LOCATION";
    }

    /**
     * Extracts product category from order data.
     */
    private String extractProductCategory(JsonNode orderData) {
        if (orderData.has("productCategory")) {
            return orderData.get("productCategory").asText();
        }
        return "BLOOD_PRODUCTS"; // Default category
    }

    /**
     * Transforms order items to the expected format.
     */
    private JsonNode transformOrderItems(JsonNode orderData) {
        try {
            JsonNode items = orderData.get("orderItems");
            if (items == null) {
                items = orderData.get("items");
            }
            
            if (items != null && items.isArray()) {
                com.fasterxml.jackson.databind.node.ArrayNode transformedItems = objectMapper.createArrayNode();
                
                for (JsonNode item : items) {
                    com.fasterxml.jackson.databind.node.ObjectNode transformedItem = objectMapper.createObjectNode();
                    
                    // Map fields to expected format
                    transformedItem.put("productFamily", 
                        item.has("productFamily") ? item.get("productFamily").asText() : "RED_BLOOD_CELLS_LEUKOREDUCED");
                    transformedItem.put("bloodType", 
                        item.has("bloodType") ? item.get("bloodType").asText() : "O-");
                    transformedItem.put("quantity", 
                        item.has("quantity") ? item.get("quantity").asInt() : 1);
                    
                    if (item.has("comments")) {
                        transformedItem.put("comments", item.get("comments").asText());
                    }
                    
                    transformedItems.add(transformedItem);
                }
                
                return transformedItems;
            }
            
            // Create default item if no items found
            com.fasterxml.jackson.databind.node.ArrayNode defaultItems = objectMapper.createArrayNode();
            com.fasterxml.jackson.databind.node.ObjectNode defaultItem = objectMapper.createObjectNode();
            defaultItem.put("productFamily", "RED_BLOOD_CELLS_LEUKOREDUCED");
            defaultItem.put("bloodType", "O-");
            defaultItem.put("quantity", 1);
            defaultItems.add(defaultItem);
            
            return defaultItems;
            
        } catch (Exception e) {
            log.warn("Failed to transform order items, using default: {}", e.getMessage());
            
            // Return default item structure
            com.fasterxml.jackson.databind.node.ArrayNode defaultItems = objectMapper.createArrayNode();
            com.fasterxml.jackson.databind.node.ObjectNode defaultItem = objectMapper.createObjectNode();
            defaultItem.put("productFamily", "RED_BLOOD_CELLS_LEUKOREDUCED");
            defaultItem.put("bloodType", "O-");
            defaultItem.put("quantity", 1);
            defaultItems.add(defaultItem);
            
            return defaultItems;
        }
    }

    /**
     * Gets or creates RSocket requester.
     */
    private RSocketRequester getRSocketRequester() {
        if (connectionManager != null && connectionManager.isConnectionAvailable()) {
            return connectionManager.getRequester();
        }
        
        // Fallback to direct connection if connection manager not available
        if (rSocketRequester == null) {
            try {
                rSocketRequester = rSocketRequesterBuilder
                    .tcp(rsocketHost, rsocketPort);
                log.info("Created direct RSocket connection to {}:{}", rsocketHost, rsocketPort);
            } catch (Exception e) {
                log.error("Failed to create RSocket connection: {}", e.getMessage());
                return null;
            }
        }
        
        return rSocketRequester;
    }

    /**
     * Fallback method for RSocket payload retrieval failures.
     */
    public CompletableFuture<PayloadResponse> fallbackGetPayload(InterfaceException exception, Exception ex) {
        log.warn("Fallback triggered for RSocket payload retrieval, externalId: {}, error: {}", 
                exception.getExternalId(), ex.getMessage());

        return CompletableFuture.completedFuture(
                PayloadResponse.builder()
                        .transactionId(exception.getTransactionId())
                        .interfaceType(exception.getInterfaceType().name())
                        .sourceService("partner-order-service-rsocket")
                        .retrieved(false)
                        .errorMessage("RSocket service unavailable: " + ex.getMessage())
                        .build());
    }

    /**
     * Fallback method for REST retry submission failures.
     */
    public CompletableFuture<ResponseEntity<Object>> fallbackSubmitRetry(InterfaceException exception, Object payload, Exception ex) {
        log.warn("Fallback triggered for REST retry submission, externalId: {}, error: {}", 
                exception.getExternalId(), ex.getMessage());

        throw new RuntimeException("REST retry service unavailable: " + ex.getMessage(), ex);
    }

    @Override
    protected String buildPayloadEndpoint(InterfaceException exception) {
        // This is used by base class REST methods, but we override getOriginalPayload to use RSocket
        return "/orders/" + exception.getExternalId();
    }

    @Override
    protected String buildRetryEndpoint(InterfaceException exception) {
        // We override submitRetry to use the configured retry URL
        return "/v1/partner-order-provider/orders";
    }

    @Override
    protected void addAuthenticationHeaders(HttpHeaders headers) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            headers.set(authHeader, apiKey);
            log.debug("Added API key authentication for partner order service");
        } else {
            log.debug("No API key configured for partner order service");
        }
    }
}