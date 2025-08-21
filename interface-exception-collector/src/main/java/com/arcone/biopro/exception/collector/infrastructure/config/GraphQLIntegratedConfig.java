package com.arcone.biopro.exception.collector.infrastructure.config;

import com.arcone.biopro.exception.collector.api.graphql.resolver.ExceptionQueryResolver;
import com.arcone.biopro.exception.collector.api.graphql.resolver.RetryMutationResolver;
import com.arcone.biopro.exception.collector.api.graphql.resolver.SummaryQueryResolver;
import com.arcone.biopro.exception.collector.api.graphql.dto.*;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import org.hibernate.Hibernate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.springframework.web.servlet.function.RequestPredicates.POST;
import static org.springframework.web.servlet.function.RouterFunctions.route;

/**
 * Integrated GraphQL configuration that connects the manual endpoint
 * with the actual GraphQL resolvers to provide real data and functionality.
 */
@Configuration
@ConditionalOnProperty(name = "graphql.features.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class GraphQLIntegratedConfig {

  private final ObjectMapper objectMapper;
  private final ExceptionQueryResolver exceptionQueryResolver;
  private final RetryMutationResolver retryMutationResolver;
  private final SummaryQueryResolver summaryQueryResolver;

  /**
   * GraphQL endpoint that integrates with actual resolvers to provide
   * real data, mutations, and subscription support.
   */
  @Bean
  public RouterFunction<ServerResponse> graphqlIntegratedRouterFunction() {
    return route(POST("/graphql"), request -> {
      try {
        // Parse the GraphQL request
        String body = request.body(String.class);
        Map<String, Object> requestMap = objectMapper.readValue(body, Map.class);
        String query = (String) requestMap.get("query");
        Map<String, Object> variables = (Map<String, Object>) requestMap.get("variables");

        log.debug("Processing GraphQL query: {}", query);
        log.debug("Variables: {}", variables);

        // Process the query with actual resolvers
        String response = processGraphQLQuery(query, variables);

        return ServerResponse.ok()
            .header("Content-Type", "application/json")
            .body(response);

      } catch (Exception e) {
        log.error("Error processing GraphQL request", e);

        String errorResponse = String.format("""
            {
              "errors": [
                {
                  "message": "GraphQL request processing error: %s",
                  "extensions": {
                    "code": "INTERNAL_ERROR"
                  }
                }
              ]
            }
            """, e.getMessage());

        return ServerResponse.status(400)
            .header("Content-Type", "application/json")
            .body(errorResponse);
      }
    });
  }

  private String processGraphQLQuery(String query, Map<String, Object> variables) throws Exception {
    if (query == null) {
      return createErrorResponse("Query is required", "VALIDATION_ERROR");
    }

    // Handle test queries
    if (query.contains("hello")) {
      return """
          {
            "data": {
              "hello": "GraphQL endpoint with full resolver integration is working!"
            }
          }
          """;
    }

    // Handle exception list queries
    if (query.contains("exceptions") && !query.contains("exception(")) {
      return handleExceptionsQuery(variables);
    }

    // Handle single exception queries
    if (query.contains("exception(")) {
      return handleExceptionQuery(variables);
    }

    // Handle summary queries
    if (query.contains("exceptionSummary")) {
      return handleSummaryQuery(variables);
    }

    // Handle mutations
    if (query.contains("mutation")) {
      return handleMutations(query, variables);
    }

    // Handle subscriptions (redirect to WebSocket)
    if (query.contains("subscription")) {
      return handleSubscriptionRedirect();
    }

    // Handle introspection
    if (query.contains("__schema") || query.contains("__type")) {
      return handleIntrospection();
    }

    return createErrorResponse("Query not recognized", "QUERY_NOT_IMPLEMENTED");
  }

  @Transactional(readOnly = true)
  private String handleExceptionsQuery(Map<String, Object> variables) throws Exception {
    log.debug("Handling exceptions query with variables: {}", variables);

    // Extract parameters
    ExceptionFilters filters = extractFromVariables(variables, "filters", ExceptionFilters.class);
    PaginationInput pagination = extractFromVariables(variables, "pagination", PaginationInput.class);
    SortingInput sorting = extractFromVariables(variables, "sorting", SortingInput.class);

    // Call actual resolver
    CompletableFuture<ExceptionConnection> future = exceptionQueryResolver.exceptions(filters, pagination, sorting);
    ExceptionConnection result = future.get();

    // Force initialization of lazy collections within transaction
    if (result != null && result.getEdges() != null) {
      for (var edge : result.getEdges()) {
        if (edge.getNode() != null) {
          // Initialize lazy collections to prevent LazyInitializationException
          initializeLazyCollections(edge.getNode());
        }
      }
    }

    // Return response
    Map<String, Object> response = Map.of("data", Map.of("exceptions", result));
    return objectMapper.writeValueAsString(response);
  }

  @Transactional(readOnly = true)
  private String handleExceptionQuery(Map<String, Object> variables) throws Exception {
    log.debug("Handling exception query with variables: {}", variables);

    String transactionId = (String) variables.get("transactionId");
    if (transactionId == null) {
      return createErrorResponse("Transaction ID is required", "VALIDATION_ERROR");
    }

    // Call actual resolver
    CompletableFuture<InterfaceException> future = exceptionQueryResolver.exception(transactionId);
    InterfaceException result = future.get();

    // Force initialization of lazy collections within transaction
    if (result != null) {
      initializeLazyCollections(result);
    }

    // Return response
    Map<String, Object> response = Map.of("data", Map.of("exception", result));
    return objectMapper.writeValueAsString(response);
  }

  private String handleSummaryQuery(Map<String, Object> variables) throws Exception {
    log.debug("Handling summary query with variables: {}", variables);

    TimeRange timeRange = extractFromVariables(variables, "timeRange", TimeRange.class);
    ExceptionFilters filters = extractFromVariables(variables, "filters", ExceptionFilters.class);

    if (timeRange == null) {
      return createErrorResponse("Time range is required", "VALIDATION_ERROR");
    }

    // Call actual resolver
    CompletableFuture<ExceptionSummary> future = summaryQueryResolver.exceptionSummary(timeRange, filters);
    ExceptionSummary result = future.get();

    // Return response
    Map<String, Object> response = Map.of("data", Map.of("exceptionSummary", result));
    return objectMapper.writeValueAsString(response);
  }

  private String handleMutations(String query, Map<String, Object> variables) throws Exception {
    log.debug("Handling mutation: {}", query);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return createErrorResponse("Authentication required for mutations", "AUTHENTICATION_ERROR");
    }

    // Handle retry exception mutation
    if (query.contains("retryException") && !query.contains("bulkRetry")) {
      RetryExceptionInput input = extractFromVariables(variables, "input", RetryExceptionInput.class);
      if (input == null) {
        return createErrorResponse("Retry input is required", "VALIDATION_ERROR");
      }

      CompletableFuture<RetryExceptionResult> future = retryMutationResolver.retryException(input, auth);
      RetryExceptionResult result = future.get();

      Map<String, Object> response = Map.of("data", Map.of("retryException", result));
      return objectMapper.writeValueAsString(response);
    }

    // Handle bulk retry mutation
    if (query.contains("bulkRetryExceptions")) {
      BulkRetryInput input = extractFromVariables(variables, "input", BulkRetryInput.class);
      if (input == null) {
        return createErrorResponse("Bulk retry input is required", "VALIDATION_ERROR");
      }

      CompletableFuture<BulkRetryResult> future = retryMutationResolver.bulkRetryExceptions(input, auth);
      BulkRetryResult result = future.get();

      Map<String, Object> response = Map.of("data", Map.of("bulkRetryExceptions", result));
      return objectMapper.writeValueAsString(response);
    }

    // Handle acknowledge exception mutation
    if (query.contains("acknowledgeException") && !query.contains("bulkAcknowledge")) {
      AcknowledgeExceptionInput input = extractFromVariables(variables, "input", AcknowledgeExceptionInput.class);
      if (input == null) {
        return createErrorResponse("Acknowledge input is required", "VALIDATION_ERROR");
      }

      CompletableFuture<AcknowledgeExceptionResult> future = retryMutationResolver.acknowledgeException(input, auth);
      AcknowledgeExceptionResult result = future.get();

      Map<String, Object> response = Map.of("data", Map.of("acknowledgeException", result));
      return objectMapper.writeValueAsString(response);
    }

    // Handle bulk acknowledge mutation
    if (query.contains("bulkAcknowledgeExceptions")) {
      BulkAcknowledgeInput input = extractFromVariables(variables, "input", BulkAcknowledgeInput.class);
      if (input == null) {
        return createErrorResponse("Bulk acknowledge input is required", "VALIDATION_ERROR");
      }

      CompletableFuture<BulkAcknowledgeResult> future = retryMutationResolver.bulkAcknowledgeExceptions(input, auth);
      BulkAcknowledgeResult result = future.get();

      Map<String, Object> response = Map.of("data", Map.of("bulkAcknowledgeExceptions", result));
      return objectMapper.writeValueAsString(response);
    }

    // Handle resolve exception mutation
    if (query.contains("resolveException")) {
      ResolveExceptionInput input = extractFromVariables(variables, "input", ResolveExceptionInput.class);
      if (input == null) {
        return createErrorResponse("Resolve input is required", "VALIDATION_ERROR");
      }

      CompletableFuture<ResolveExceptionResult> future = retryMutationResolver.resolveException(input, auth);
      ResolveExceptionResult result = future.get();

      Map<String, Object> response = Map.of("data", Map.of("resolveException", result));
      return objectMapper.writeValueAsString(response);
    }

    // Handle cancel retry mutation
    if (query.contains("cancelRetry")) {
      String transactionId = (String) variables.get("transactionId");
      String reason = (String) variables.get("reason");

      if (transactionId == null || reason == null) {
        return createErrorResponse("Transaction ID and reason are required", "VALIDATION_ERROR");
      }

      CompletableFuture<CancelRetryResult> future = retryMutationResolver.cancelRetry(transactionId, reason, auth);
      CancelRetryResult result = future.get();

      Map<String, Object> response = Map.of("data", Map.of("cancelRetry", result));
      return objectMapper.writeValueAsString(response);
    }

    return createErrorResponse("Unknown mutation operation", "MUTATION_NOT_FOUND");
  }

  private String handleSubscriptionRedirect() {
    return """
        {
          "errors": [
            {
              "message": "Subscriptions must use WebSocket connection at /subscriptions endpoint",
              "extensions": {
                "code": "SUBSCRIPTION_TRANSPORT_ERROR",
                "websocketUrl": "/subscriptions",
                "protocol": "graphql-ws"
              }
            }
          ]
        }
        """;
  }

  private String handleIntrospection() {
    return """
        {
          "data": {
            "__schema": {
              "types": [
                {
                  "name": "Query",
                  "description": "Root query type with full resolver integration"
                },
                {
                  "name": "Mutation",
                  "description": "Root mutation type with full resolver integration"
                },
                {
                  "name": "Subscription",
                  "description": "Root subscription type for real-time updates via WebSocket"
                }
              ]
            }
          }
        }
        """;
  }

  @SuppressWarnings("unchecked")
  private <T> T extractFromVariables(Map<String, Object> variables, String key, Class<T> type) {
    if (variables == null)
      return null;
    Object value = variables.get(key);
    if (value == null)
      return null;

    try {
      return objectMapper.convertValue(value, type);
    } catch (Exception e) {
      log.warn("Failed to convert variable '{}' to type {}: {}", key, type.getSimpleName(), e.getMessage());
      return null;
    }
  }

  private String createErrorResponse(String message, String code) {
    try {
      Map<String, Object> error = Map.of(
          "message", message,
          "extensions", Map.of("code", code));
      return objectMapper.writeValueAsString(Map.of("errors", java.util.List.of(error)));
    } catch (Exception e) {
      return "{\"errors\":[{\"message\":\"Internal error creating error response\"}]}";
    }
  }

  /**
   * Initialize lazy collections to prevent LazyInitializationException
   * when serializing entities outside of Hibernate session.
   */
  private void initializeLazyCollections(InterfaceException exception) {
    try {
      // Initialize retry attempts collection
      if (exception.getRetryAttempts() != null) {
        Hibernate.initialize(exception.getRetryAttempts());
      }

      // Initialize order items collection
      if (exception.getOrderItems() != null) {
        Hibernate.initialize(exception.getOrderItems());
      }

      // Initialize status changes collection
      if (exception.getStatusChanges() != null) {
        Hibernate.initialize(exception.getStatusChanges());
      }

      log.debug("Initialized lazy collections for exception: {}", exception.getTransactionId());

    } catch (Exception e) {
      log.warn("Failed to initialize lazy collections for exception {}: {}",
          exception.getTransactionId(), e.getMessage());
    }
  }
}