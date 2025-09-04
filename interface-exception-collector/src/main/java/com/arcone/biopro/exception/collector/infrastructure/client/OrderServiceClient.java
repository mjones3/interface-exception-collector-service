package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client for interacting with the Order Service to retrieve payloads
 * and submit retry requests for order-related exceptions.
 * This client is only active when the mock RSocket server is disabled.
 */
@Component
@ConditionalOnProperty(name = "app.rsocket.mock-server.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class OrderServiceClient extends BaseSourceServiceClient {

    @Value("${app.source-services.order.api-key:#{null}}")
    private String apiKey;

    @Value("${app.source-services.order.auth-header:X-API-Key}")
    private String authHeader;

    public OrderServiceClient(RestTemplate restTemplate,
            @Value("${app.source-services.order.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "order-service");
    }

    @Override
    public boolean supports(String interfaceType) {
        return InterfaceType.ORDER.name().equals(interfaceType);
    }

    @Override
    protected String buildPayloadEndpoint(InterfaceException exception) {
        return "/api/v1/orders/" + exception.getTransactionId() + "/payload";
    }

    @Override
    protected String buildRetryEndpoint(InterfaceException exception) {
        return "/api/v1/orders/" + exception.getTransactionId() + "/retry";
    }

    @Override
    protected void addAuthenticationHeaders(HttpHeaders headers) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            headers.set(authHeader, apiKey);
            log.debug("Added API key authentication for order service");
        }
    }
}