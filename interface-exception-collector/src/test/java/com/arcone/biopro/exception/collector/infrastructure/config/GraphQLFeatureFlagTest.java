package com.arcone.biopro.exception.collector.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class to verify GraphQL feature flags work correctly.
 * Tests that feature properties are loaded and configured properly.
 */
@SpringBootTest(classes = { GraphQLFeatureProperties.class })
@ActiveProfiles("test")
class GraphQLFeatureFlagTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load feature properties with default values")
    void shouldLoadFeaturePropertiesWithDefaults() {
        // Verify feature properties are loaded
        assertThat(applicationContext.containsBean("graphQLFeatureProperties")).isTrue();

        GraphQLFeatureProperties featureProperties = applicationContext.getBean(GraphQLFeatureProperties.class);
        assertThat(featureProperties).isNotNull();
        assertThat(featureProperties.isEnabled()).isTrue();
        assertThat(featureProperties.isQueryEnabled()).isTrue();
        assertThat(featureProperties.isMutationEnabled()).isTrue();
        assertThat(featureProperties.isSubscriptionEnabled()).isTrue();
        assertThat(featureProperties.hasAnyEndpointEnabled()).isTrue();
        assertThat(featureProperties.isCompletelyDisabled()).isFalse();
    }

    @SpringBootTest(classes = { GraphQLFeatureProperties.class })
    @TestPropertySource(properties = {
            "graphql.features.enabled=false"
    })
    @ActiveProfiles("test")
    static class GraphQLDisabledTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("Should load feature properties when GraphQL is disabled")
        void shouldLoadFeaturePropertiesWhenDisabled() {
            GraphQLFeatureProperties featureProperties = applicationContext.getBean(GraphQLFeatureProperties.class);
            assertThat(featureProperties).isNotNull();
            assertThat(featureProperties.isEnabled()).isFalse();
            assertThat(featureProperties.isCompletelyDisabled()).isTrue();
        }
    }

    @SpringBootTest(classes = { GraphQLFeatureProperties.class })
    @TestPropertySource(properties = {
            "graphql.features.enabled=true",
            "graphql.features.query-enabled=false",
            "graphql.features.mutation-enabled=false",
            "graphql.features.subscription-enabled=false"
    })
    @ActiveProfiles("test")
    static class GraphQLEndpointsDisabledTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("Should disable endpoints when all endpoints disabled")
        void shouldDisableEndpointsWhenAllDisabled() {
            GraphQLFeatureProperties featureProperties = applicationContext.getBean(GraphQLFeatureProperties.class);
            assertThat(featureProperties).isNotNull();
            assertThat(featureProperties.isEnabled()).isTrue();
            assertThat(featureProperties.hasAnyEndpointEnabled()).isFalse();
            assertThat(featureProperties.isQueryEnabled()).isFalse();
            assertThat(featureProperties.isMutationEnabled()).isFalse();
            assertThat(featureProperties.isSubscriptionEnabled()).isFalse();
        }
    }

    @SpringBootTest(classes = { GraphQLFeatureProperties.class })
    @TestPropertySource(properties = {
            "graphql.features.enabled=true",
            "graphql.features.graphiql-enabled=false",
            "graphql.features.introspection-enabled=false",
            "graphql.features.query-allowlist-enabled=true"
    })
    @ActiveProfiles("test")
    static class ProductionSecurityModeTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("Should enable production security mode when configured")
        void shouldEnableProductionSecurityMode() {
            GraphQLFeatureProperties featureProperties = applicationContext.getBean(GraphQLFeatureProperties.class);
            assertThat(featureProperties).isNotNull();
            assertThat(featureProperties.isProductionSecurityMode()).isTrue();
            assertThat(featureProperties.isDevelopmentMode()).isFalse();
            assertThat(featureProperties.isGraphiqlEnabled()).isFalse();
            assertThat(featureProperties.isIntrospectionEnabled()).isFalse();
            assertThat(featureProperties.isQueryAllowlistEnabled()).isTrue();
        }
    }

    @SpringBootTest(classes = { GraphQLFeatureProperties.class })
    @TestPropertySource(properties = {
            "graphql.features.enabled=true",
            "graphql.features.graphiql-enabled=true",
            "graphql.features.introspection-enabled=true",
            "graphql.features.development-tools-enabled=true"
    })
    @ActiveProfiles("test")
    static class DevelopmentModeTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("Should enable development mode when configured")
        void shouldEnableDevelopmentMode() {
            GraphQLFeatureProperties featureProperties = applicationContext.getBean(GraphQLFeatureProperties.class);
            assertThat(featureProperties).isNotNull();
            assertThat(featureProperties.isDevelopmentMode()).isTrue();
            assertThat(featureProperties.isProductionSecurityMode()).isFalse();
            assertThat(featureProperties.isGraphiqlEnabled()).isTrue();
            assertThat(featureProperties.isIntrospectionEnabled()).isTrue();
            assertThat(featureProperties.isDevelopmentToolsEnabled()).isTrue();
        }
    }
}