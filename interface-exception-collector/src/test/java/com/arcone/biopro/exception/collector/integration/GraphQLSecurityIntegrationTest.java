package com.arcone.biopro.exception.collector.integration;

import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for GraphQL security covering JWT
 * authentication,
 * role-based authorization, CORS configuration, and GraphQL-specific security
 * features.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("GraphQL Security Integration Tests")
class GraphQLSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private final String jwtSecret = "mySecretKey1234567890123456789012345678901234567890";
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("GraphQL Endpoint JWT Authentication")
    class GraphQLEndpointJwtAuthenticationTests {

        @Test
        @DisplayName("Should successfully access GraphQL endpoint with valid OPERATOR token")
        void shouldSuccessfullyAccessGraphQLEndpointWithValidOperatorToken() throws Exception {
            String token = createValidToken("operator", List.of("OPERATOR"));
            String query = createSimpleGraphQLQuery();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should successfully access GraphQL endpoint with valid VIEWER token")
        void shouldSuccessfullyAccessGraphQLEndpointWithValidViewerToken() throws Exception {
            String token = createValidToken("viewer", List.of("VIEWER"));
            String query = createSimpleGraphQLQuery();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should successfully access GraphQL endpoint with valid ADMIN token")
        void shouldSuccessfullyAccessGraphQLEndpointWithValidAdminToken() throws Exception {
            String token = createValidToken("admin", List.of("ADMIN"));
            String query = createSimpleGraphQLQuery();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject GraphQL access without token")
        void shouldRejectGraphQLAccessWithoutToken() throws Exception {
            String query = createSimpleGraphQLQuery();

            mockMvc.perform(post("/graphql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject GraphQL access with expired token")
        void shouldRejectGraphQLAccessWithExpiredToken() throws Exception {
            String expiredToken = createExpiredToken("operator", List.of("OPERATOR"));
            String query = createSimpleGraphQLQuery();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + expiredToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject GraphQL access with malformed token")
        void shouldRejectGraphQLAccessWithMalformedToken() throws Exception {
            String malformedToken = "invalid.jwt.token";
            String query = createSimpleGraphQLQuery();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + malformedToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GraphQL Subscription WebSocket Authentication")
    class GraphQLSubscriptionWebSocketAuthenticationTests {

        @Test
        @DisplayName("Should successfully access subscriptions endpoint with valid token")
        void shouldSuccessfullyAccessSubscriptionsEndpointWithValidToken() throws Exception {
            String token = createValidToken("operator", List.of("OPERATOR"));

            mockMvc.perform(get("/subscriptions")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject subscriptions access without token")
        void shouldRejectSubscriptionsAccessWithoutToken() throws Exception {
            mockMvc.perform(get("/subscriptions"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject subscriptions access with expired token")
        void shouldRejectSubscriptionsAccessWithExpiredToken() throws Exception {
            String expiredToken = createExpiredToken("operator", List.of("OPERATOR"));

            mockMvc.perform(get("/subscriptions")
                    .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GraphiQL Development Interface Access")
    class GraphiQLDevelopmentInterfaceAccessTests {

        @Test
        @DisplayName("Should allow access to GraphiQL interface without token")
        void shouldAllowAccessToGraphiQLInterfaceWithoutToken() throws Exception {
            mockMvc.perform(get("/graphiql/index.html"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to GraphiQL interface with invalid token")
        void shouldAllowAccessToGraphiQLInterfaceWithInvalidToken() throws Exception {
            mockMvc.perform(get("/graphiql/index.html")
                    .header("Authorization", "Bearer invalid.token"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to GraphiQL resources without authentication")
        void shouldAllowAccessToGraphiQLResourcesWithoutAuthentication() throws Exception {
            mockMvc.perform(get("/graphiql/"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GraphQL CORS Configuration")
    class GraphQLCorsConfigurationTests {

        @Test
        @DisplayName("Should handle CORS preflight request for GraphQL endpoint")
        void shouldHandleCorsPreflightRequestForGraphQLEndpoint() throws Exception {
            mockMvc.perform(options("/graphql")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "POST")
                    .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                    .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }

        @Test
        @DisplayName("Should handle CORS preflight request for subscriptions endpoint")
        void shouldHandleCorsPreflightRequestForSubscriptionsEndpoint() throws Exception {
            mockMvc.perform(options("/subscriptions")
                    .header("Origin", "https://dashboard.biopro.com")
                    .header("Access-Control-Request-Method", "GET")
                    .header("Access-Control-Request-Headers", "Authorization"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "https://dashboard.biopro.com"))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }

        @Test
        @DisplayName("Should handle CORS preflight request for GraphiQL endpoint")
        void shouldHandleCorsPreflightRequestForGraphiQLEndpoint() throws Exception {
            mockMvc.perform(options("/graphiql/")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
        }

        @Test
        @DisplayName("Should include CORS headers in GraphQL responses")
        void shouldIncludeCorsHeadersInGraphQLResponses() throws Exception {
            String token = createValidToken("operator", List.of("OPERATOR"));
            String query = createSimpleGraphQLQuery();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + token)
                    .header("Origin", "http://localhost:3000")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }

        @Test
        @DisplayName("Should reject CORS requests from unauthorized origins")
        void shouldRejectCorsRequestsFromUnauthorizedOrigins() throws Exception {
            String token = createValidToken("operator", List.of("OPERATOR"));
            String query = createSimpleGraphQLQuery();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + token)
                    .header("Origin", "https://malicious-site.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GraphQL Role-Based Authorization")
    class GraphQLRoleBasedAuthorizationTests {

        @Test
        @DisplayName("Should allow VIEWER role to access query operations")
        void shouldAllowViewerRoleToAccessQueryOperations() throws Exception {
            String viewerToken = createValidToken("viewer", List.of("VIEWER"));
            String queryOperation = createGraphQLQueryOperation();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + viewerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(queryOperation))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow OPERATOR role to access both query and mutation operations")
        void shouldAllowOperatorRoleToAccessBothQueryAndMutationOperations() throws Exception {
            String operatorToken = createValidToken("operator", List.of("OPERATOR"));

            // Test query operation
            String queryOperation = createGraphQLQueryOperation();
            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + operatorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(queryOperation))
                    .andExpect(status().isOk());

            // Test mutation operation
            String mutationOperation = createGraphQLMutationOperation();
            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + operatorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mutationOperation))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow ADMIN role to access all GraphQL operations")
        void shouldAllowAdminRoleToAccessAllGraphQLOperations() throws Exception {
            String adminToken = createValidToken("admin", List.of("ADMIN"));

            // Test query operation
            String queryOperation = createGraphQLQueryOperation();
            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(queryOperation))
                    .andExpect(status().isOk());

            // Test mutation operation
            String mutationOperation = createGraphQLMutationOperation();
            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mutationOperation))
                    .andExpect(status().isOk());

            // Test subscription operation
            String subscriptionOperation = createGraphQLSubscriptionOperation();
            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(subscriptionOperation))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle token with multiple roles for GraphQL operations")
        void shouldHandleTokenWithMultipleRolesForGraphQLOperations() throws Exception {
            String multiRoleToken = createValidToken("multiuser", List.of("VIEWER", "OPERATOR", "ADMIN"));
            String queryOperation = createGraphQLQueryOperation();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + multiRoleToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(queryOperation))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should deny access for users with no roles")
        void shouldDenyAccessForUsersWithNoRoles() throws Exception {
            String noRoleToken = createValidToken("noroleuser", List.of());
            String query = createSimpleGraphQLQuery();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + noRoleToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Security Headers and Configuration")
    class SecurityHeadersAndConfigurationTests {

        @Test
        @DisplayName("Should include security headers in GraphQL responses")
        void shouldIncludeSecurityHeadersInGraphQLResponses() throws Exception {
            String token = createValidToken("operator", List.of("OPERATOR"));
            String query = createSimpleGraphQLQuery();

            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-Frame-Options", "DENY"))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                    .andExpect(header().exists("Strict-Transport-Security"));
        }

        @Test
        @DisplayName("Should validate GraphQL content type requirements")
        void shouldValidateGraphQLContentTypeRequirements() throws Exception {
            String token = createValidToken("operator", List.of("OPERATOR"));
            String query = createSimpleGraphQLQuery();

            // Should accept application/json
            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isOk());

            // Should reject other content types
            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(query))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("Unified Security with REST API")
    class UnifiedSecurityWithRestApiTests {

        @Test
        @DisplayName("Should use same JWT token for both REST and GraphQL endpoints")
        void shouldUseSameJwtTokenForBothRestAndGraphQLEndpoints() throws Exception {
            String token = createValidToken("operator", List.of("OPERATOR"));

            // Test REST endpoint
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            // Test GraphQL endpoint with same token
            String query = createSimpleGraphQLQuery();
            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(query))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should apply same role-based authorization to both APIs")
        void shouldApplySameRoleBasedAuthorizationToBothApis() throws Exception {
            String viewerToken = createValidToken("viewer", List.of("VIEWER"));

            // VIEWER should be able to read from REST
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + viewerToken))
                    .andExpect(status().isOk());

            // VIEWER should be able to query from GraphQL
            String queryOperation = createGraphQLQueryOperation();
            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + viewerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(queryOperation))
                    .andExpect(status().isOk());

            // VIEWER should not be able to write to REST
            mockMvc.perform(post("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + viewerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should maintain consistent security context between REST and GraphQL")
        void shouldMaintainConsistentSecurityContextBetweenRestAndGraphQL() throws Exception {
            String adminToken = createValidToken("admin", List.of("ADMIN"));

            // Admin should access admin endpoints in REST
            mockMvc.perform(get("/actuator/metrics")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            // Admin should access all GraphQL operations
            String queryOperation = createGraphQLQueryOperation();
            mockMvc.perform(post("/graphql")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(queryOperation))
                    .andExpect(status().isOk());
        }
    }

    // Helper methods for creating test tokens (same as JWT integration test)
    private String createValidToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }

    private String createExpiredToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }

    // Helper methods for creating GraphQL queries
    private String createSimpleGraphQLQuery() throws Exception {
        Map<String, Object> request = Map.of(
                "query", "{ systemHealth { status } }");
        return objectMapper.writeValueAsString(request);
    }

    private String createGraphQLQueryOperation() throws Exception {
        Map<String, Object> request = Map.of(
                "query",
                "query GetExceptions { exceptions(pagination: { first: 10 }) { totalCount edges { node { id transactionId status } } } }");
        return objectMapper.writeValueAsString(request);
    }

    private String createGraphQLMutationOperation() throws Exception {
        Map<String, Object> request = Map.of(
                "query",
                "mutation RetryException($input: RetryExceptionInput!) { retryException(input: $input) { success errors { message } } }",
                "variables", Map.of(
                        "input", Map.of(
                                "transactionId", "test-transaction-id",
                                "reason", "Test retry")));
        return objectMapper.writeValueAsString(request);
    }

    private String createGraphQLSubscriptionOperation() throws Exception {
        Map<String, Object> request = Map.of(
                "query",
                "subscription ExceptionUpdates { exceptionUpdated { eventType exception { id transactionId } } }");
        return objectMapper.writeValueAsString(request);
    }
}