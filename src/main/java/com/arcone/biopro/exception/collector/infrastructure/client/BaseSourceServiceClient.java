package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * Base implementation for source service clients with common functionality
 * including circuit breaker, retry, and timeout patterns.
 */
@RequiredArgsConstructor
@Slf4j
public abstract class BaseSourceServiceClient implements SourceServiceClient {

    protected final RestTemplate restTemplate;
    protected final String baseUrl;
    protected final String serviceName;

    @Override
    @CircuitBreaker(name = "source-service", fallbackMethod = "fallbackGetPayload")
    @TimeLimiter(name = "source-service")
    @Retry(name = "source-service")
    public CompletableFuture<PayloadResponse> getOriginalPayload(InterfaceException exception) {
        log.info("Retrieving original payload for transaction: {}, service: {}",
                exception.getTransactionId(), serviceName);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = buildPayloadEndpoint(exception);
                String fullUrl = baseUrl + endpoint;

                log.debug("Calling source service: {}", fullUrl);

                HttpHeaders headers = createHeaders(exception);
                HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

                ResponseEntity<Object> response = restTemplate.exchange(
                        fullUrl,
                        HttpMethod.GET,
                        requestEntity,
                        Object.class);

                log.info("Successfully retrieved payload for transaction: {}", exception.getTransactionId());

                return PayloadResponse.builder()
                        .transactionId(exception.getTransactionId())
                        .interfaceType(exception.getInterfaceType().name())
                        .payload(response.getBody())
                        .sourceService(serviceName)
                        .retrieved(true)
                        .build();

            } catch (Exception e) {
                log.error("Failed to retrieve payload for transaction: {}, error: {}",
                        exception.getTransactionId(), e.getMessage());

                return PayloadResponse.builder()
                        .transactionId(exception.getTransactionId())
                        .interfaceType(exception.getInterfaceType().name())
                        .sourceService(serviceName)
                        .retrieved(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        });
    }

    @Override
    @CircuitBreaker(name = "source-service", fallbackMethod = "fallbackSubmitRetry")
    @TimeLimiter(name = "source-service")
    @Retry(name = "source-service")
    public CompletableFuture<ResponseEntity<Object>> submitRetry(InterfaceException exception, Object payload) {
        log.info("Submitting retry for transaction: {}, service: {}",
                exception.getTransactionId(), serviceName);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = buildRetryEndpoint(exception);
                String fullUrl = baseUrl + endpoint;

                log.debug("Submitting retry to: {}", fullUrl);

                HttpHeaders headers = createRetryHeaders(exception);
                HttpEntity<Object> requestEntity = new HttpEntity<>(payload, headers);

                ResponseEntity<Object> response = restTemplate.exchange(
                        fullUrl,
                        getRetryHttpMethod(exception),
                        requestEntity,
                        Object.class);

                log.info("Successfully submitted retry for transaction: {}, status: {}",
                        exception.getTransactionId(), response.getStatusCode());

                return response;

            } catch (Exception e) {
                log.error("Failed to submit retry for transaction: {}, error: {}",
                        exception.getTransactionId(), e.getMessage());
                throw new RuntimeException("Retry submission failed: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Fallback method when circuit breaker is open or service calls fail.
     */
    public CompletableFuture<PayloadResponse> fallbackGetPayload(InterfaceException exception, Exception ex) {
        log.warn("Fallback triggered for payload retrieval, transaction: {}, service: {}, error: {}",
                exception.getTransactionId(), serviceName, ex.getMessage());

        return CompletableFuture.completedFuture(
                PayloadResponse.builder()
                        .transactionId(exception.getTransactionId())
                        .interfaceType(exception.getInterfaceType().name())
                        .sourceService(serviceName)
                        .retrieved(false)
                        .errorMessage("Service unavailable - circuit breaker open or service failure")
                        .build());
    }

    /**
     * Fallback method for retry submission failures.
     */
    public CompletableFuture<ResponseEntity<Object>> fallbackSubmitRetry(InterfaceException exception, Object payload,
            Exception ex) {
        log.warn("Fallback triggered for retry submission, transaction: {}, service: {}, error: {}",
                exception.getTransactionId(), serviceName, ex.getMessage());

        throw new RuntimeException("Retry service unavailable - circuit breaker open or service failure", ex);
    }

    /**
     * Creates HTTP headers for service requests including authentication.
     */
    protected HttpHeaders createHeaders(InterfaceException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Correlation-ID", exception.getTransactionId());
        headers.set("User-Agent", "interface-exception-collector-service/1.0");

        // Add authentication headers if configured
        addAuthenticationHeaders(headers);

        return headers;
    }

    /**
     * Creates HTTP headers for retry requests.
     */
    protected HttpHeaders createRetryHeaders(InterfaceException exception) {
        HttpHeaders headers = createHeaders(exception);
        headers.set("X-Retry-Attempt", "true");
        headers.set("X-Retry-Count", String.valueOf(exception.getRetryCount() + 1));
        return headers;
    }

    /**
     * Adds authentication headers to the request.
     * Override in subclasses for service-specific authentication.
     */
    protected void addAuthenticationHeaders(HttpHeaders headers) {
        // Default implementation - no authentication
        // Override in subclasses for service-specific authentication
    }

    /**
     * Determines the HTTP method for retry requests based on operation type.
     */
    protected HttpMethod getRetryHttpMethod(InterfaceException exception) {
        if (exception.getOperation().contains("CREATE")) {
            return HttpMethod.POST;
        } else if (exception.getOperation().contains("MODIFY") || exception.getOperation().contains("UPDATE")) {
            return HttpMethod.PUT;
        } else {
            return HttpMethod.POST; // Default to POST for retries
        }
    }

    /**
     * Builds the endpoint URL for payload retrieval.
     * Must be implemented by subclasses for service-specific endpoints.
     */
    protected abstract String buildPayloadEndpoint(InterfaceException exception);

    /**
     * Builds the endpoint URL for retry requests.
     * Must be implemented by subclasses for service-specific endpoints.
     */
    protected abstract String buildRetryEndpoint(InterfaceException exception);
}