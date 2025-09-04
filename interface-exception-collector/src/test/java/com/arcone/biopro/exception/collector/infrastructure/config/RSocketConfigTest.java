package com.arcone.biopro.exception.collector.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RSocket configuration.
 */
@SpringBootTest(classes = RSocketConfig.class)
@TestPropertySource(properties = {
    "biopro.rsocket.mock-server.enabled=true"
})
class RSocketConfigTest {

    @Test
    void shouldCreateRSocketStrategies() {
        // Given
        RSocketConfig config = new RSocketConfig();

        // When
        RSocketStrategies strategies = config.rSocketStrategies();

        // Then
        assertThat(strategies).isNotNull();
    }

    @Test
    void shouldCreateRSocketRequesterBuilder() {
        // Given
        RSocketConfig config = new RSocketConfig();
        RSocketStrategies strategies = config.rSocketStrategies();

        // When
        RSocketRequester.Builder builder = config.rSocketRequesterBuilder(strategies);

        // Then
        assertThat(builder).isNotNull();
    }
}