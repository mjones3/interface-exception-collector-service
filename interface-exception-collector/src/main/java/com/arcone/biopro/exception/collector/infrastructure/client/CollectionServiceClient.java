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
 * Client for interacting with the Collection Service to retrieve payloads
 * and submit retry requests for collection-related exceptions.
 */
@Component
@ConditionalOnProperty(name = "app.source-services.collection.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class CollectionServiceClient extends BaseSourceServiceClient {

    @Value("${app.source-services.collection.api-key:#{null}}")
    private String apiKey;

    @Value("${app.source-services.collection.auth-header:X-API-Key}")
    private String authHeader;

    public CollectionServiceClient(RestTemplate restTemplate,
            @Value("${app.source-services.collection.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "collection-service");
    }

    @Override
    public boolean supports(String interfaceType) {
        return InterfaceType.COLLECTION.name().equals(interfaceType);
    }

    @Override
    protected String buildPayloadEndpoint(InterfaceException exception) {
        return "/api/v1/collections/" + exception.getTransactionId() + "/payload";
    }

    @Override
    protected String buildRetryEndpoint(InterfaceException exception) {
        return "/api/v1/collections/" + exception.getTransactionId() + "/retry";
    }

    @Override
    protected void addAuthenticationHeaders(HttpHeaders headers) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            headers.set(authHeader, apiKey);
            log.debug("Added API key authentication for collection service");
        }
    }
}