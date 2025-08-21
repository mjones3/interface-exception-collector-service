package com.arcone.biopro.exception.collector.infrastructure.config;

import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Optional;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;

/**
 * GraphQL development configuration for enhanced development experience.
 * Provides schema introspection, documentation endpoints, and development
 * tools.
 */
@Configuration
@Profile({ "dev", "local", "test" })
@ConditionalOnProperty(name = "graphql.development.enabled", havingValue = "true", matchIfMissing = false)
public class GraphQLDevelopmentConfig {

  private static final Logger logger = LoggerFactory.getLogger(GraphQLDevelopmentConfig.class);

  /**
   * Provides enhanced GraphQL instrumentation for development.
   * Includes tracing and performance monitoring.
   */
  @Bean
  @ConditionalOnProperty(name = "graphql.development.tracing.enabled", havingValue = "true", matchIfMissing = true)
  public Instrumentation graphqlDevelopmentInstrumentation() {
    logger.info("Enabling GraphQL development instrumentation with tracing");

    return new ChainedInstrumentation(List.of(
        new TracingInstrumentation()));
  }

  /**
   * Provides a router function for GraphQL schema documentation endpoint.
   * Accessible at /graphql/schema for development purposes.
   */
  @Bean
  @ConditionalOnProperty(name = "graphql.development.schema-endpoint.enabled", havingValue = "true", matchIfMissing = false)
  public RouterFunction<ServerResponse> graphqlSchemaRouter(Optional<GraphQlSource> graphQlSource) {
    return route(GET("/graphql/schema"), request -> {
      try {
        if (graphQlSource.isEmpty()) {
          return ServerResponse.status(503)
              .body("GraphQL schema not available");
        }
        GraphQLSchema schema = graphQlSource.get().schema();
        SchemaPrinter schemaPrinter = new SchemaPrinter(
            SchemaPrinter.Options.defaultOptions()
                .includeIntrospectionTypes(false)
                .includeScalarTypes(true)
                .includeScalarTypes(true)
                .includeSchemaDefinition(true)
                .includeDirectives(true));

        String schemaString = schemaPrinter.print(schema);

        return ServerResponse.ok()
            .header("Content-Type", "text/plain; charset=utf-8")
            .body(schemaString);

      } catch (Exception e) {
        logger.error("Error generating GraphQL schema documentation", e);
        return ServerResponse.status(500)
            .body("Error generating schema: " + e.getMessage());
      }
    });
  }

  /**
   * Provides a router function for GraphQL development information endpoint.
   * Accessible at /graphql/info for development purposes.
   */
  @Bean
  public RouterFunction<ServerResponse> graphqlInfoRouter() {
    return route(GET("/graphql/info"), request -> {
      String info = """
          # GraphQL Development Information

          ## Available Endpoints
          - **GraphQL Endpoint**: `/graphql` (POST)
          - **GraphiQL Interface**: `/graphiql` (GET)
          - **WebSocket Subscriptions**: `/subscriptions` (WebSocket)
          - **Schema Documentation**: `/graphql/schema` (GET)
          - **Development Info**: `/graphql/info` (GET)

          ## Available Operations

          ### Queries
          - `exceptions(filters, pagination, sorting)` - List exceptions with filtering and pagination
          - `exception(transactionId)` - Get exception details by transaction ID
          - `exceptionSummary(timeRange, filters)` - Get aggregated exception statistics

          ### Mutations
          - `retryException(transactionId, reason, priority)` - Retry a failed exception
          - `acknowledgeException(transactionId, reason, notes)` - Acknowledge an exception
          - `resolveException(transactionId, resolution, notes)` - Mark exception as resolved
          - `bulkRetryExceptions(transactionIds, reason, priority)` - Retry multiple exceptions
          - `bulkAcknowledgeExceptions(transactionIds, reason, notes)` - Acknowledge multiple exceptions

          ### Subscriptions
          - `exceptionUpdates(filters)` - Real-time exception updates
          - `retryStatusUpdates(transactionId)` - Real-time retry status updates
          - `summaryUpdates(timeRange)` - Real-time summary statistics updates

          ## Example Queries

          ### Basic Exception List
          ```graphql
          query {
            exceptions(
              filters: {
                interfaceTypes: [ORDER, COLLECTION]
                statuses: [NEW, ACKNOWLEDGED]
                severities: [HIGH, CRITICAL]
              }
              pagination: {
                first: 10
              }
              sorting: {
                field: "timestamp"
                direction: DESC
              }
            ) {
              edges {
                node {
                  transactionId
                  interfaceType
                  exceptionReason
                  status
                  severity
                  timestamp
                }
              }
              pageInfo {
                hasNextPage
                hasPreviousPage
              }
              totalCount
            }
          }
          ```

          ### Exception Details with Related Data
          ```graphql
          query {
            exception(transactionId: "TXN-12345") {
              transactionId
              interfaceType
              exceptionReason
              status
              severity
              retryHistory {
                attemptNumber
                status
                initiatedBy
                initiatedAt
                resultSuccess
              }
              originalPayload {
                content
                contentType
                sourceService
              }
              statusHistory {
                previousStatus
                newStatus
                changedBy
                changedAt
                reason
              }
            }
          }
          ```

          ### Retry Exception Mutation
          ```graphql
          mutation {
            retryException(
              transactionId: "TXN-12345"
              reason: "Manual retry after system fix"
              priority: HIGH
            ) {
              success
              message
              exception {
                transactionId
                status
                retryCount
              }
              retryAttempt {
                attemptNumber
                status
                initiatedBy
              }
            }
          }
          ```

          ### Real-time Exception Updates Subscription
          ```graphql
          subscription {
            exceptionUpdates(
              filters: {
                interfaceTypes: [ORDER]
                severities: [HIGH, CRITICAL]
              }
            ) {
              transactionId
              status
              interfaceType
              severity
              updateType
              timestamp
            }
          }
          ```

          ## Authentication
          All GraphQL operations require JWT authentication via the `Authorization: Bearer <token>` header.

          ## Rate Limiting
          - ADMIN: 300 requests/minute, 10,000 requests/hour
          - OPERATOR: 120 requests/minute, 3,600 requests/hour
          - VIEWER: 60 requests/minute, 1,800 requests/hour

          ## Query Complexity Limits
          - Maximum query depth: 15 (development), 10 (production)
          - Maximum query complexity: 2000 (development), 1000 (production)

          ## Development Features
          - Schema introspection enabled
          - GraphiQL interface available
          - Query tracing enabled
          - Enhanced error messages
          - Performance metrics collection
          """;

      return ServerResponse.ok()
          .header("Content-Type", "text/markdown; charset=utf-8")
          .body(info);
    });
  }

  /**
   * Provides a router function for GraphQL query examples endpoint.
   * Accessible at /graphql/examples for development purposes.
   */
  @Bean
  public RouterFunction<ServerResponse> graphqlExamplesRouter() {
    return route(GET("/graphql/examples"), request -> {
      String examples = """
          {
            "queries": {
              "listExceptions": {
                "description": "List exceptions with filtering and pagination",
                "query": "query ListExceptions($filters: ExceptionFilters, $pagination: PaginationInput, $sorting: SortingInput) {\\n  exceptions(filters: $filters, pagination: $pagination, sorting: $sorting) {\\n    edges {\\n      node {\\n        transactionId\\n        interfaceType\\n        exceptionReason\\n        status\\n        severity\\n        timestamp\\n      }\\n    }\\n    pageInfo {\\n      hasNextPage\\n      hasPreviousPage\\n    }\\n    totalCount\\n  }\\n}",
                "variables": {
                  "filters": {
                    "interfaceTypes": ["ORDER"],
                    "statuses": ["NEW", "ACKNOWLEDGED"],
                    "severities": ["HIGH", "CRITICAL"]
                  },
                  "pagination": {
                    "first": 10
                  },
                  "sorting": {
                    "field": "timestamp",
                    "direction": "DESC"
                  }
                }
              },
              "getException": {
                "description": "Get exception details by transaction ID",
                "query": "query GetException($transactionId: String!) {\\n  exception(transactionId: $transactionId) {\\n    transactionId\\n    interfaceType\\n    exceptionReason\\n    status\\n    severity\\n    retryHistory {\\n      attemptNumber\\n      status\\n      initiatedBy\\n      initiatedAt\\n    }\\n  }\\n}",
                "variables": {
                  "transactionId": "TXN-12345"
                }
              },
              "exceptionSummary": {
                "description": "Get exception summary statistics",
                "query": "query ExceptionSummary($timeRange: TimeRange!) {\\n  exceptionSummary(timeRange: $timeRange) {\\n    totalExceptions\\n    byInterfaceType {\\n      interfaceType\\n      count\\n      percentage\\n    }\\n    bySeverity {\\n      severity\\n      count\\n      percentage\\n    }\\n    keyMetrics {\\n      retrySuccessRate\\n      averageResolutionTime\\n      criticalExceptions\\n    }\\n  }\\n}",
                "variables": {
                  "timeRange": {
                    "start": "2024-01-01T00:00:00Z",
                    "end": "2024-12-31T23:59:59Z"
                  }
                }
              }
            },
            "mutations": {
              "retryException": {
                "description": "Retry a failed exception",
                "query": "mutation RetryException($transactionId: String!, $reason: String!, $priority: RetryPriority!) {\\n  retryException(transactionId: $transactionId, reason: $reason, priority: $priority) {\\n    success\\n    message\\n    exception {\\n      transactionId\\n      status\\n      retryCount\\n    }\\n    retryAttempt {\\n      attemptNumber\\n      status\\n      initiatedBy\\n    }\\n  }\\n}",
                "variables": {
                  "transactionId": "TXN-12345",
                  "reason": "Manual retry after system fix",
                  "priority": "HIGH"
                }
              },
              "acknowledgeException": {
                "description": "Acknowledge an exception",
                "query": "mutation AcknowledgeException($transactionId: String!, $reason: String!, $notes: String) {\\n  acknowledgeException(transactionId: $transactionId, reason: $reason, notes: $notes) {\\n    success\\n    message\\n    exception {\\n      transactionId\\n      status\\n      acknowledgedBy\\n      acknowledgedAt\\n    }\\n  }\\n}",
                "variables": {
                  "transactionId": "TXN-12345",
                  "reason": "Issue acknowledged by operations team",
                  "notes": "Investigating root cause"
                }
              },
              "bulkRetry": {
                "description": "Retry multiple exceptions",
                "query": "mutation BulkRetry($transactionIds: [String!]!, $reason: String!, $priority: RetryPriority!) {\\n  bulkRetryExceptions(transactionIds: $transactionIds, reason: $reason, priority: $priority) {\\n    totalRequested\\n    successCount\\n    failureCount\\n    results {\\n      transactionId\\n      success\\n      message\\n    }\\n  }\\n}",
                "variables": {
                  "transactionIds": ["TXN-001", "TXN-002", "TXN-003"],
                  "reason": "Bulk retry after system maintenance",
                  "priority": "NORMAL"
                }
              }
            },
            "subscriptions": {
              "exceptionUpdates": {
                "description": "Real-time exception updates",
                "query": "subscription ExceptionUpdates($filters: ExceptionFilters) {\\n  exceptionUpdates(filters: $filters) {\\n    transactionId\\n    status\\n    interfaceType\\n    severity\\n    updateType\\n    timestamp\\n  }\\n}",
                "variables": {
                  "filters": {
                    "interfaceTypes": ["ORDER"],
                    "severities": ["HIGH", "CRITICAL"]
                  }
                }
              },
              "retryStatusUpdates": {
                "description": "Real-time retry status updates",
                "query": "subscription RetryStatusUpdates($transactionId: String!) {\\n  retryStatusUpdates(transactionId: $transactionId) {\\n    transactionId\\n    attemptNumber\\n    status\\n    initiatedBy\\n    initiatedAt\\n    completedAt\\n    resultSuccess\\n  }\\n}",
                "variables": {
                  "transactionId": "TXN-12345"
                }
              }
            }
          }
          """;

      return ServerResponse.ok()
          .header("Content-Type", "application/json; charset=utf-8")
          .body(examples);
    });
  }
}