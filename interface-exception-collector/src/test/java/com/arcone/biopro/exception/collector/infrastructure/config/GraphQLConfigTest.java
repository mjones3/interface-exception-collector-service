package com.arcone.biopro.exception.collector.infrastructure.config;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for GraphQL configuration to verify that all GraphQL
 * infrastructure
 * components are properly configured and working.
 */
class GraphQLConfigTest {

    @Test
    void shouldCreateMaxQueryComplexityInstrumentation() {
        // Given
        GraphQLConfig config = new GraphQLConfig();

        // When
        MaxQueryComplexityInstrumentation instrumentation = config.maxQueryComplexityInstrumentation();

        // Then
        assertThat(instrumentation).isNotNull();
    }

    @Test
    void shouldCreateMaxQueryDepthInstrumentation() {
        // Given
        GraphQLConfig config = new GraphQLConfig();

        // When
        MaxQueryDepthInstrumentation instrumentation = config.maxQueryDepthInstrumentation();

        // Then
        assertThat(instrumentation).isNotNull();
    }

    @Test
    void shouldCreateRuntimeWiringConfigurer() {
        // Given
        GraphQLConfig config = new GraphQLConfig();

        // When
        RuntimeWiringConfigurer configurer = config.runtimeWiringConfigurer();

        // Then
        assertThat(configurer).isNotNull();
    }

    @Test
    void shouldCreateGraphiqlConfiguration() {
        // Given
        GraphQLConfig config = new GraphQLConfig();

        // When
        String graphiqlConfig = config.graphiqlConfiguration();

        // Then
        assertThat(graphiqlConfig).isEqualTo("graphiql-enabled");
    }
}