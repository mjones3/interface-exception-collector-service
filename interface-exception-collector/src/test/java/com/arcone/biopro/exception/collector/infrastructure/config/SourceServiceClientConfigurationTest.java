package com.arcone.biopro.exception.collector.infrastructure.config;

import com.arcone.biopro.exception.collector.infrastructure.client.MockRSocketOrderServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.client.OrderServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for SourceServiceClientConfiguration to verify proper conditional bean registration.
 */
@SpringBootTest
class SourceServiceClientConfigurationTest {

    @MockBean
    private RSocketRequester.Builder rSocketRequesterBuilder;

    /**
     * Test configuration with mock server enabled (development profile).
     */
    @SpringBootTest
    @ActiveProfiles("dev")
    @TestPropertySource(properties = {
        "app.rsocket.mock-server.enabled=true",
        "app.source-services.order.base-url=http://test-order-service:8090"
    })
    static class MockServerEnabledTest {

        @Test
        void shouldCreateMockRSocketOrderServiceClient(ApplicationContext context) {
            // When mock server is enabled, should create MockRSocketOrderServiceClient
            SourceServiceClient client = context.getBean(SourceServiceClient.class);
            
            assertThat(client).isInstanceOf(MockRSocketOrderServiceClient.class);
            assertThat(client.supports("ORDER")).isTrue();
            assertThat(client.getServiceName()).isEqualTo("mock-rsocket-server");
        }
    }

    /**
     * Test configuration with mock server disabled (production profile).
     */
    @SpringBootTest
    @ActiveProfiles("prod")
    @TestPropertySource(properties = {
        "app.rsocket.mock-server.enabled=false",
        "app.source-services.order.base-url=http://partner-order-service:8090"
    })
    static class MockServerDisabledTest {

        @Test
        void shouldCreateProductionOrderServiceClient(ApplicationContext context) {
            // When mock server is disabled, should create OrderServiceClient
            SourceServiceClient client = context.getBean(SourceServiceClient.class);
            
            assertThat(client).isInstanceOf(OrderServiceClient.class);
            assertThat(client.supports("ORDER")).isTrue();
            assertThat(client.getServiceName()).isEqualTo("order-service");
        }
    }

    /**
     * Test configuration properties binding.
     */
    @SpringBootTest
    @TestPropertySource(properties = {
        "app.rsocket.mock-server.enabled=true",
        "app.rsocket.mock-server.host=test-host",
        "app.rsocket.mock-server.port=7001",
        "app.rsocket.mock-server.timeout=10s",
        "app.features.debug-mode=true",
        "app.features.circuit-breaker=false"
    })
    static class ConfigurationPropertiesTest {

        @Test
        void shouldBindRSocketProperties(ApplicationContext context) {
            RSocketProperties properties = context.getBean(RSocketProperties.class);
            
            assertThat(properties.getMockServer().isEnabled()).isTrue();
            assertThat(properties.getMockServer().getHost()).isEqualTo("test-host");
            assertThat(properties.getMockServer().getPort()).isEqualTo(7001);
            assertThat(properties.getMockServer().getTimeout().getSeconds()).isEqualTo(10);
        }

        @Test
        void shouldBindFeatureFlagsProperties(ApplicationContext context) {
            FeatureFlagsProperties properties = context.getBean(FeatureFlagsProperties.class);
            
            assertThat(properties.isDebugMode()).isTrue();
            assertThat(properties.isCircuitBreaker()).isFalse();
        }
    }
}