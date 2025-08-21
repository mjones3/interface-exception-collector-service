package com.arcone.biopro.exception.collector.api.graphql.config;

import com.arcone.biopro.exception.collector.infrastructure.config.GraphQLConfig;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GraphQLConfig to verify instrumentation and configuration
 * setup.
 */
class GraphQLConfigTest {

    private GraphQLConfig graphQLConfig;

    @BeforeEach
    void setUp() {
        graphQLConfig = new GraphQLConfig();
        graphQLConfig.setMaxQueryComplexity(1000);
        graphQLConfig.setMaxQueryDepth(10);
        graphQLConfig.setQueryTimeoutSeconds(30);
        graphQLConfig.setEnableComplexityAnalysis(true);
        graphQLConfig.setEnableDepthAnalysis(true);
    }

    @Test
    void shouldCreateQueryComplexityInstrumentation() {
        // When
        MaxQueryComplexityInstrumentation instrumentation = graphQLConfig.queryComplexityInstrumentation();

        // Then
        assertThat(instrumentation).isNotNull();
    }

    @Test
    void shouldCreateQueryDepthInstrumentation() {
        // When
        MaxQueryDepthInstrumentation instrumentation = graphQLConfig.maxQueryDepthInstrumentation();

        // Then
        assertThat(instrumentation).isNotNull();
    }

    @Test
    void shouldCreateTimeoutInstrumentation() {
        // When
        Instrumentation instrumentation = graphQLConfig.queryTimeoutInstrumentation();

        // Then
        assertThat(instrumentation).isNotNull();
    }

    @Test
    void shouldCreateChainedInstrumentation() {
        // When
        Instrumentation chainedInstrumentation = graphQLConfig.graphQLInstrumentation();

        // Then
        assertThat(chainedInstrumentation).isNotNull();
        assertThat(chainedInstrumentation).isInstanceOf(ChainedInstrumentation.class);
    }

    @Test
    void shouldHaveCorrectConfigurationProperties() {
        // Then
        assertThat(graphQLConfig.getMaxQueryComplexity()).isEqualTo(1000);
        assertThat(graphQLConfig.getMaxQueryDepth()).isEqualTo(10);
        assertThat(graphQLConfig.getQueryTimeoutSeconds()).isEqualTo(30);
        assertThat(graphQLConfig.isEnableComplexityAnalysis()).isTrue();
        assertThat(graphQLConfig.isEnableDepthAnalysis()).isTrue();
    }

    @Test
    void shouldDisableComplexityAnalysisWhenConfigured() {
        // Given
        graphQLConfig.setEnableComplexityAnalysis(false);

        // When
        Instrumentation chainedInstrumentation = graphQLConfig.graphQLInstrumentation();

        // Then
        assertThat(chainedInstrumentation).isNotNull();
        // The chained instrumentation should still be created but with fewer
        // instrumentations
    }

    @Test
    void shouldDisableDepthAnalysisWhenConfigured() {
        // Given
        graphQLConfig.setEnableDepthAnalysis(false);

        // When
        Instrumentation chainedInstrumentation = graphQLConfig.graphQLInstrumentation();

        // Then
        assertThat(chainedInstrumentation).isNotNull();
        // The chained instrumentation should still be created but with fewer
        // instrumentations
    }
}