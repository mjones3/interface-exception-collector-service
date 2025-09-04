package com.arcone.biopro.exception.collector.infrastructure.config;

import com.arcone.biopro.exception.collector.infrastructure.client.MockRSocketOrderServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.client.OrderServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.client.RSocketConnectionManager;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for source service clients with conditional bean registration
 * based on feature flags and environment settings.
 */
@Configuration
@EnableConfigurationProperties({RSocketProperties.class, FeatureFlagsProperties.class})
@RequiredArgsConstructor
@Slf4j
public class SourceServiceClientConfiguration {

    private final RSocketProperties rSocketProperties;
    private final FeatureFlagsProperties featureFlagsProperties;
    private final Environment environment;

    /**
     * Creates the mock RSocket order service client when mock server is enabled.
     * This client takes priority over the production order service client.
     *
     * @param restTemplate the REST template for HTTP calls
     * @param rSocketRequesterBuilder the RSocket requester builder
     * @return the mock RSocket order service client
     */
    @Bean
    @ConditionalOnProperty(name = "app.rsocket.mock-server.enabled", havingValue = "true")
    @Primary
    public SourceServiceClient mockRSocketOrderServiceClient(RestTemplate restTemplate,
                                                            RSocketRequester.Builder rSocketRequesterBuilder,
                                                            RSocketConnectionManager connectionManager) {
        String activeProfile = getActiveProfile();
        
        // Additional safety check - never use mock in production
        if (isProductionEnvironment(activeProfile)) {
            log.warn("Mock RSocket server is enabled but running in production environment. " +
                    "This configuration will be ignored for safety.");
            throw new IllegalStateException("Mock RSocket server cannot be enabled in production environment");
        }
        
        log.info("Configuring Mock RSocket Order Service Client - Environment: {}, Host: {}, Port: {}", 
                activeProfile, 
                rSocketProperties.getMockServer().getHost(), 
                rSocketProperties.getMockServer().getPort());
        
        if (featureFlagsProperties.shouldEnableDebugLogging(activeProfile)) {
            log.debug("Mock RSocket server configuration: {}", rSocketProperties.getMockServer());
        }
        
        return new MockRSocketOrderServiceClient(restTemplate, rSocketRequesterBuilder, rSocketProperties, connectionManager);
    }

    /**
     * Creates the production order service client when mock server is disabled.
     * This is the default client for production environments.
     *
     * @param restTemplate the REST template for HTTP calls
     * @param baseUrl the base URL for the order service
     * @return the production order service client
     */
    @Bean
    @ConditionalOnProperty(name = "app.rsocket.mock-server.enabled", havingValue = "false", matchIfMissing = true)
    public SourceServiceClient productionOrderServiceClient(RestTemplate restTemplate,
            @Value("${app.source-services.order.base-url}") String baseUrl) {
        String activeProfile = getActiveProfile();
        
        log.info("Configuring Production Order Service Client - Environment: {}, Base URL: {}", 
                activeProfile, baseUrl);
        
        if (featureFlagsProperties.shouldEnableDebugLogging(activeProfile)) {
            log.debug("Partner order service configuration: {}", rSocketProperties.getPartnerOrderService());
        }
        
        return new OrderServiceClient(restTemplate, baseUrl);
    }

    /**
     * Gets the active Spring profile.
     *
     * @return the active profile or "default" if none set
     */
    private String getActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length > 0 ? activeProfiles[0] : "default";
    }

    /**
     * Checks if the current environment is production.
     *
     * @param profile the active profile
     * @return true if production environment
     */
    private boolean isProductionEnvironment(String profile) {
        return "prod".equalsIgnoreCase(profile) || 
               "production".equalsIgnoreCase(profile) ||
               profile.toLowerCase().contains("prod");
    }
}