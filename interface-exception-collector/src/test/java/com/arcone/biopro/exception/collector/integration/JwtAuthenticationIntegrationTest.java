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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * Comprehensive integration tests for JWT authentication covering end-to-end
 * authentication flows
 * Requirements: 1.1, 1.2, 1.3, 4.1
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("JWT Authentication Integration Tests")
class JwtAuthenticationIntegrationTest {

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
    @DisplayName("Protected Endpoint Access with Valid Tokens")
    class ProtectedEndpointAccessWithValidTokensTests {

        @Test
        @DisplayName("Should successfully access exceptions endpoint with valid OPERATIONS token")
        void shouldSuccessfullyAccessExceptionsEndpointWithValidOperationsToken() throws Exception {
            String token = createValidToken("operator", List.of("OPERATIONS"));

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should successfully access exceptions endpoint with valid VIEWER token")
        void shouldSuccessfullyAccessExceptionsEndpointWithValidViewerToken() throws Exception {
            String token = createValidToken("viewer", List.of("VIEWER"));

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should successfully access exceptions endpoint with valid ADMIN token")
        void shouldSuccessfullyAccessExceptionsEndpointWithValidAdminToken() throws Exception {
            String token = createValidToken("admin", List.of("ADMIN"));

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should successfully access specific exception with valid token")
        void shouldSuccessfullyAccessSpecificExceptionWithValidToken() throws Exception {
            String token = createValidToken("operator", List.of("OPERATIONS"));

            mockMvc.perform(get("/api/v1/exceptions/test-id")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should successfully access admin endpoints with ADMIN token")
        void shouldSuccessfullyAccessAdminEndpointsWithAdminToken() throws Exception {
            String token = createValidToken("admin", List.of("ADMIN"));

            mockMvc.perform(get("/actuator/metrics")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle token with multiple roles")
        void shouldHandleTokenWithMultipleRoles() throws Exception {
            String token = createValidToken("multiuser", List.of("VIEWER", "OPERATIONS", "ADMIN"));

            // Should be able to access all endpoints
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/actuator/metrics")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Protected Endpoint Access with Invalid/Expired Tokens")
    class ProtectedEndpointAccessWithInvalidTokensTests {

        @Test
        @DisplayName("Should reject access with expired token")
        void shouldRejectAccessWithExpiredToken() throws Exception {
            String expiredToken = createExpiredToken("operator", List.of("OPERATIONS"));

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject access with malformed token")
        void shouldRejectAccessWithMalformedToken() throws Exception {
            String malformedToken = "invalid.jwt.token";

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + malformedToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject access with token signed with wrong key")
        void shouldRejectAccessWithTokenSignedWithWrongKey() throws Exception {
            String tokenWithWrongSignature = createTokenWithWrongSignature("operator", List.of("OPERATIONS"));

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + tokenWithWrongSignature))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject access with empty token")
        void shouldRejectAccessWithEmptyToken() throws Exception {
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer "))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject access with non-Bearer token")
        void shouldRejectAccessWithNonBearerToken() throws Exception {
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Basic dXNlcjpwYXNz"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject access with token without subject")
        void shouldRejectAccessWithTokenWithoutSubject() throws Exception {
            String tokenWithoutSubject = createTokenWithoutSubject(List.of("OPERATIONS"));

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + tokenWithoutSubject))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject access with token with empty subject")
        void shouldRejectAccessWithTokenWithEmptySubject() throws Exception {
            String tokenWithEmptySubject = createTokenWithEmptySubject(List.of("OPERATIONS"));

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + tokenWithEmptySubject))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Public Endpoint Access Without Tokens")
    class PublicEndpointAccessWithoutTokensTests {

        @Test
        @DisplayName("Should allow access to health endpoint without token")
        void shouldAllowAccessToHealthEndpointWithoutToken() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to info endpoint without token")
        void shouldAllowAccessToInfoEndpointWithoutToken() throws Exception {
            mockMvc.perform(get("/actuator/info"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to swagger UI without token")
        void shouldAllowAccessToSwaggerUIWithoutToken() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to API docs without token")
        void shouldAllowAccessToApiDocsWithoutToken() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to public endpoints even with invalid token")
        void shouldAllowAccessToPublicEndpointsEvenWithInvalidToken() throws Exception {
            mockMvc.perform(get("/actuator/health")
                    .header("Authorization", "Bearer invalid.token"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/actuator/info")
                    .header("Authorization", "Bearer invalid.token"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Role-Based Access Control Functionality")
    class RoleBasedAccessControlFunctionalityTests {

        @Test
        @DisplayName("Should allow VIEWER role to read but deny write operations")
        void shouldAllowViewerRoleToReadButDenyWriteOperations() throws Exception {
            String viewerToken = createValidToken("viewer", List.of("VIEWER"));

            // Should allow read operations
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + viewerToken))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/exceptions/test-id")
                    .header("Authorization", "Bearer " + viewerToken))
                    .andExpect(status().isOk());

            // Should deny write operations
            mockMvc.perform(post("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + viewerToken)
                    .contentType("application/json")
                    .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/v1/exceptions/test-id/retry")
                    .header("Authorization", "Bearer " + viewerToken)
                    .contentType("application/json")
                    .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(put("/api/v1/exceptions/test-id/acknowledge")
                    .header("Authorization", "Bearer " + viewerToken)
                    .contentType("application/json")
                    .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(put("/api/v1/exceptions/test-id/resolve")
                    .header("Authorization", "Bearer " + viewerToken)
                    .contentType("application/json")
                    .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow OPERATIONS role to read and write operations")
        void shouldAllowOperationsRoleToReadAndWriteOperations() throws Exception {
            String operationsToken = createValidToken("operator", List.of("OPERATIONS"));

            // Should allow read operations
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + operationsToken))
                    .andExpect(status().isOk());

            // Should allow write operations (may return different status codes based on
            // business logic)
            mockMvc.perform(post("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + operationsToken)
                    .contentType("application/json")
                    .content("{}"))
                    .andExpect(status().isBadRequest()); // Assuming invalid request body

            mockMvc.perform(post("/api/v1/exceptions/test-id/retry")
                    .header("Authorization", "Bearer " + operationsToken)
                    .contentType("application/json")
                    .content("{}"))
                    .andExpect(status().isNotFound()); // Assuming test-id doesn't exist

            mockMvc.perform(put("/api/v1/exceptions/test-id/acknowledge")
                    .header("Authorization", "Bearer " + operationsToken)
                    .contentType("application/json")
                    .content("{}"))
                    .andExpect(status().isNotFound()); // Assuming test-id doesn't exist
        }

        @Test
        @DisplayName("Should allow ADMIN role to access all endpoints including admin-only")
        void shouldAllowAdminRoleToAccessAllEndpointsIncludingAdminOnly() throws Exception {
            String adminToken = createValidToken("admin", List.of("ADMIN"));

            // Should allow read operations
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            // Should allow write operations
            mockMvc.perform(post("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType("application/json")
                    .content("{}"))
                    .andExpect(status().isBadRequest()); // Assuming invalid request body

            // Should allow admin-only endpoints
            mockMvc.perform(get("/actuator/metrics")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should deny non-admin roles from accessing admin endpoints")
        void shouldDenyNonAdminRolesFromAccessingAdminEndpoints() throws Exception {
            String viewerToken = createValidToken("viewer", List.of("VIEWER"));
            String operationsToken = createValidToken("operator", List.of("OPERATIONS"));

            mockMvc.perform(get("/actuator/metrics")
                    .header("Authorization", "Bearer " + viewerToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/actuator/metrics")
                    .header("Authorization", "Bearer " + operationsToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should deny access for users with no roles")
        void shouldDenyAccessForUsersWithNoRoles() throws Exception {
            String noRoleToken = createValidToken("noroleuser", List.of());

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + noRoleToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should handle case-insensitive role matching")
        void shouldHandleCaseInsensitiveRoleMatching() throws Exception {
            String lowerCaseRoleToken = createValidToken("user", List.of("operations", "viewer"));

            // Should work with lowercase roles (assuming the system normalizes them)
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + lowerCaseRoleToken))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Authentication Error Handling")
    class AuthenticationErrorHandlingTests {

        @Test
        @DisplayName("Should return proper error response for missing token")
        void shouldReturnProperErrorResponseForMissingToken() throws Exception {
            mockMvc.perform(get("/api/v1/exceptions"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return proper error response for expired token")
        void shouldReturnProperErrorResponseForExpiredToken() throws Exception {
            String expiredToken = createExpiredToken("operator", List.of("OPERATIONS"));

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return proper error response for invalid signature")
        void shouldReturnProperErrorResponseForInvalidSignature() throws Exception {
            String tokenWithWrongSignature = createTokenWithWrongSignature("operator", List.of("OPERATIONS"));

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + tokenWithWrongSignature))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return proper error response for insufficient privileges")
        void shouldReturnProperErrorResponseForInsufficientPrivileges() throws Exception {
            String viewerToken = createValidToken("viewer", List.of("VIEWER"));

            mockMvc.perform(post("/api/v1/exceptions/test-id/retry")
                    .header("Authorization", "Bearer " + viewerToken)
                    .contentType("application/json")
                    .content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Token Format and Structure Tests")
    class TokenFormatAndStructureTests {

        @Test
        @DisplayName("Should handle token with extra whitespace")
        void shouldHandleTokenWithExtraWhitespace() throws Exception {
            String token = createValidToken("operator", List.of("OPERATIONS"));
            String tokenWithWhitespace = "  " + token + "  ";

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + tokenWithWhitespace))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle token with different case Bearer prefix")
        void shouldHandleTokenWithDifferentCaseBearerPrefix() throws Exception {
            String token = createValidToken("operator", List.of("OPERATIONS"));

            // Should work with proper case
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            // Should not work with different case (case-sensitive)
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "bearer " + token))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "BEARER " + token))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should validate token structure and claims")
        void shouldValidateTokenStructureAndClaims() throws Exception {
            String validToken = createValidToken("testuser", List.of("OPERATIONS"));

            // Verify the token is properly structured by using it successfully
            mockMvc.perform(get("/api/v1/exceptions")
                    .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk());

            // Verify that the JWT service can validate the same token
            var claims = jwtService.validateToken(validToken);
            assert claims.getSubject().equals("testuser");
            assert claims.get("roles").equals(List.of("OPERATIONS"));
        }
    }

    // Helper methods for creating test tokens
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

    private String createTokenWithWrongSignature(String username, List<String> roles) {
        SecretKey wrongKey = Keys
                .hmacShaKeyFor("wrongSecretKey123456789012345678901234567890".getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(wrongKey)
                .compact();
    }

    private String createTokenWithoutSubject(List<String> roles) {
        return Jwts.builder()
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }

    private String createTokenWithEmptySubject(List<String> roles) {
        return Jwts.builder()
                .subject("")
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }
}