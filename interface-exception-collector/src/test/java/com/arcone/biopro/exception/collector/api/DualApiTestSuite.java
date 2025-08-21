package com.arcone.biopro.exception.collector.api;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Comprehensive test suite for dual API (REST + GraphQL) functionality.
 * 
 * This test suite validates:
 * - GraphQL resolver unit tests
 * - GraphQL security integration
 * - GraphQL subscription functionality
 * - Data consistency between REST and GraphQL APIs
 * - Security consistency between both APIs
 * - Performance comparison between both APIs
 * - Error handling consistency between both APIs
 * - GraphQL development tools and workflow
 * 
 * Run this suite to verify complete dual API integration.
 */
@Suite
@SuiteDisplayName("Dual API Integration Test Suite")
@SelectPackages({
        "com.arcone.biopro.exception.collector.api",
        "com.arcone.biopro.exception.collector.api.graphql"
})
@IncludeClassNamePatterns({
        ".*GraphQLResolverUnitTest",
        ".*GraphQLSecurityIntegrationTest",
        ".*GraphQLSubscriptionSecurityTest",
        ".*DualApiEquivalenceTest",
        ".*DualApiSecurityConsistencyTest",
        ".*DualApiPerformanceComparisonTest",
        ".*DualApiErrorHandlingConsistencyTest",
        ".*GraphQLDevelopmentWorkflowTest"
})
public class DualApiTestSuite {
    // Test suite configuration class
    // Individual test classes are automatically discovered and executed
}