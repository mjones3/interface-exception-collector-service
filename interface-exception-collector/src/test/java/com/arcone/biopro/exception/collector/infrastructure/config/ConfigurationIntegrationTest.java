package com.arcone.biopro.exception.collector.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test to verify configuration loads correctly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.rsocket.mock-server.enabled=true",
    "app.rsocket.mock-server.host=localhost",
    "app.rsocket.mock-server.port=7000",
    "app.source-services.order.base-url=http://test-order-service:8090",
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ConfigurationIntegrationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // with our new configuration classes
    }
}