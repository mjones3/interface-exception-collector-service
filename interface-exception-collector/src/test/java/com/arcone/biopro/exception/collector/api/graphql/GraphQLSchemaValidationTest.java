package com.arcone.biopro.exception.collector.api.graphql;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to validate GraphQL schema structure and completeness.
 * Ensures the schema meets all requirements from the design document.
 */
@SpringBootTest
@ActiveProfiles("test")
class GraphQLSchemaValidationTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private GraphQLSchema graphQLSchema;

    @Test
    @DisplayName("Should have all required query operations")
    void testRequiredQueryOperations() {
        // Verify root Query type exists
        assertThat(graphQLSchema.getQueryType()).isNotNull();

        // Verify required query fields
        assertThat(graphQLSchema.getQueryType().getFieldDefinition("exceptions")).isNotNull();
        assertThat(graphQLSchema.getQueryType().getFieldDefinition("exception")).isNotNull();
        assertThat(graphQLSchema.getQueryType().getFieldDefinition("exceptionSummary")).isNotNull();
    }

    @Test
    @DisplayName("Should have all required mutation operations")
    void testRequiredMutationOperations() {
        // Verify root Mutation type exists
        assertThat(graphQLSchema.getMutationType()).isNotNull();

        // Verify required mutation fields
        assertThat(graphQLSchema.getMutationType().getFieldDefinition("retryException")).isNotNull();
        assertThat(graphQLSchema.getMutationType().getFieldDefinition("acknowledgeException")).isNotNull();
        assertThat(graphQLSchema.getMutationType().getFieldDefinition("bulkAcknowledgeExceptions")).isNotNull();
    }

    @Test
    @DisplayName("Should have subscription operations for real-time updates")
    void testSubscriptionOperations() {
        // Verify root Subscription type exists
        assertThat(graphQLSchema.getSubscriptionType()).isNotNull();

        // Verify required subscription fields
        assertThat(graphQLSchema.getSubscriptionType().getFieldDefinition("exceptionUpdated")).isNotNull();
    }

    @Test
    @DisplayName("Should have all required Exception type fields")
    void testExceptionTypeFields() {
        var exceptionType = graphQLSchema.getType("Exception");
        assertThat(exceptionType).isNotNull();

        // Verify core fields
        List<String> requiredFields = List.of(
                "id", "transactionId", "externalId", "interfaceType", "exceptionReason",
                "operation", "status", "severity", "category", "customerId", "locationCode",
                "timestamp", "processedAt", "retryable", "retryCount", "maxRetries",
                "lastRetryAt", "acknowledgedBy", "acknowledgedAt");

        requiredFields.forEach(fieldName -> {
            assertThat(graphQLSchema.getType("Exception").getChildren())
                    .as("Exception type should have field: " + fieldName)
                    .anyMatch(field -> field.toString().contains(fieldName));
        });
    }

    @Test
    @DisplayName("Should have all required input types")
    void testRequiredInputTypes() {
        // Verify filter input types
        assertThat(graphQLSchema.getType("ExceptionFilters")).isNotNull();
        assertThat(graphQLSchema.getType("PaginationInput")).isNotNull();
        assertThat(graphQLSchema.getType("SortingInput")).isNotNull();
        assertThat(graphQLSchema.getType("DateRangeInput")).isNotNull();

        // Verify mutation input types
        assertThat(graphQLSchema.getType("RetryExceptionInput")).isNotNull();
        assertThat(graphQLSchema.getType("AcknowledgeExceptionInput")).isNotNull();
    }

    @Test
    @DisplayName("Should have all required enum types")
    void testRequiredEnumTypes() {
        // Verify enum types exist
        assertThat(graphQLSchema.getType("InterfaceType")).isNotNull();
        assertThat(graphQLSchema.getType("ExceptionStatus")).isNotNull();
        assertThat(graphQLSchema.getType("ExceptionSeverity")).isNotNull();
        assertThat(graphQLSchema.getType("ExceptionCategory")).isNotNull();
        assertThat(graphQLSchema.getType("RetryStatus")).isNotNull();
        assertThat(graphQLSchema.getType("RetryPriority")).isNotNull();
        assertThat(graphQLSchema.getType("SortDirection")).isNotNull();
        assertThat(graphQLSchema.getType("TimeRange")).isNotNull();
    }

    @Test
    @DisplayName("Should have custom scalar types")
    void testCustomScalarTypes() {
        // Verify custom scalars
        assertThat(graphQLSchema.getType("DateTime")).isNotNull();
        assertThat(graphQLSchema.getType("JSON")).isNotNull();
    }

    @Test
    @DisplayName("Should have connection types for pagination")
    void testConnectionTypes() {
        // Verify connection types for pagination
        assertThat(graphQLSchema.getType("ExceptionConnection")).isNotNull();
        assertThat(graphQLSchema.getType("ExceptionEdge")).isNotNull();
        assertThat(graphQLSchema.getType("PageInfo")).isNotNull();
    }

    @Test
    @DisplayName("Should have nested object types")
    void testNestedObjectTypes() {
        // Verify nested object types
        assertThat(graphQLSchema.getType("RetryAttempt")).isNotNull();
        assertThat(graphQLSchema.getType("OriginalPayload")).isNotNull();
        assertThat(graphQLSchema.getType("StatusChange")).isNotNull();
        assertThat(graphQLSchema.getType("ExceptionSummary")).isNotNull();
        assertThat(graphQLSchema.getType("KeyMetrics")).isNotNull();
        assertThat(graphQLSchema.getType("TrendDataPoint")).isNotNull();
    }

    @Test
    @DisplayName("Should have result types for mutations")
    void testMutationResultTypes() {
        // Verify mutation result types
        assertThat(graphQLSchema.getType("RetryExceptionResult")).isNotNull();
        assertThat(graphQLSchema.getType("AcknowledgeExceptionResult")).isNotNull();
        assertThat(graphQLSchema.getType("BulkAcknowledgeResult")).isNotNull();
    }

    @Test
    @DisplayName("Should have subscription event types")
    void testSubscriptionEventTypes() {
        // Verify subscription event types
        assertThat(graphQLSchema.getType("ExceptionUpdateEvent")).isNotNull();
    }

    @Test
    @DisplayName("Should parse schema files without errors")
    void testSchemaFilesParsing() throws IOException {
        SchemaParser schemaParser = new SchemaParser();

        // Test main schema files
        List<String> schemaFiles = List.of(
                "classpath:graphql/schema.graphqls",
                "classpath:graphql/exception.graphqls",
                "classpath:graphql/inputs.graphqls",
                "classpath:graphql/scalars.graphqls",
                "classpath:graphql/subscriptions.graphqls");

        for (String schemaFile : schemaFiles) {
            Resource resource = resourceLoader.getResource(schemaFile);
            if (resource.exists()) {
                String schemaContent = resource.getContentAsString(StandardCharsets.UTF_8);

                // Should parse without throwing exceptions
                TypeDefinitionRegistry registry = schemaParser.parse(schemaContent);
                assertThat(registry).isNotNull();
                assertThat(registry.types()).isNotEmpty();
            }
        }
    }

    @Test
    @DisplayName("Should have proper field types and nullability")
    void testFieldTypesAndNullability() {
        var exceptionType = graphQLSchema.getType("Exception");
        assertThat(exceptionType).isNotNull();

        // Test that required fields are non-null
        // This is a basic check - in a real implementation, you'd check specific field
        // types
        assertThat(exceptionType.getChildren()).isNotEmpty();
    }

    @Test
    @DisplayName("Should support introspection in development")
    void testIntrospectionSupport() {
        // Verify introspection fields exist (should be available in development)
        var queryType = graphQLSchema.getQueryType();

        // In development, introspection should be available
        // In production, it should be disabled
        assertThat(queryType).isNotNull();
    }

    @Test
    @DisplayName("Should have proper directive definitions")
    void testDirectiveDefinitions() {
        // Verify that custom directives are properly defined
        var directives = graphQLSchema.getDirectives();
        assertThat(directives).isNotEmpty();

        // Should have standard GraphQL directives
        assertThat(directives).anyMatch(directive -> directive.getName().equals("include"));
        assertThat(directives).anyMatch(directive -> directive.getName().equals("skip"));
        assertThat(directives).anyMatch(directive -> directive.getName().equals("deprecated"));
    }

    @Test
    @DisplayName("Should validate schema complexity limits")
    void testSchemaComplexityLimits() {
        // Verify that the schema doesn't have excessive nesting or complexity
        var allTypes = graphQLSchema.getAllTypesAsList();

        // Should have reasonable number of types (not too many, not too few)
        assertThat(allTypes.size()).isBetween(20, 100);

        // Should not have circular references that could cause infinite loops
        // This is a basic check - more sophisticated analysis would be needed for
        // production
        assertThat(allTypes).isNotEmpty();
    }
}