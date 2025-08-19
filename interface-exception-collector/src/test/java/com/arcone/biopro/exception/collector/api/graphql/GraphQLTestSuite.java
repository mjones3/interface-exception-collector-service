package com.arcone.biopro.exception.collector.api.graphql;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Comprehensive test suite for all GraphQL components.
 * Runs all unit tests for resolvers, services, security, validation, and data
 * loaders.
 */
@Suite
@SuiteDisplayName("GraphQL API Test Suite")
@SelectPackages({
        "com.arcone.biopro.exception.collector.api.graphql.resolver",
        "com.arcone.biopro.exception.collector.api.graphql.service",
        "com.arcone.biopro.exception.collector.api.graphql.security",
        "com.arcone.biopro.exception.collector.api.graphql.validation",
        "com.arcone.biopro.exception.collector.api.graphql.dataloader",
        "com.arcone.biopro.exception.collector.api.graphql.exception",
        "com.arcone.biopro.exception.collector.api.graphql.config"
})
public class GraphQLTestSuite {
    // Test suite configuration - no implementation needed
}