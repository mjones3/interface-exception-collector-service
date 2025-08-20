package com.arcone.biopro.exception.collector.config;

import com.arcone.biopro.exception.collector.infrastructure.config.SecurityConfig;
import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtService;
import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtAuthenticationFilter;
import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtAuthenticationEntryPoint;
import com.arcone.biopro.exception.collector.infrastructure.config.security.JwtAccessDeniedHandler;
import com.arcone.biopro.exception.collector.infrastructure.config.security.GraphQLAuthenticationProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Comprehensive security configuration tests covering all authentication and
 * authorization scenarios
 * Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Security Configuration Tests")
class SecurityConfigTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private final String jwtSecret = "mySecretKey1234567890123456789012345678901234567890";

        @Nested
        @DisplayName("Public Endpoint Access Tests")
        class PublicEndpointAccessTests {

                @Test
                @DisplayName("Should allow access to health endpoint without authentication")
                void shouldAllowAccessToHealthEndpoint() throws Exception {
                        mockMvc.perform(get("/actuator/health"))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Should allow access to info endpoint without authentication")
                void shouldAllowAccessToInfoEndpoint() throws Exception {
                        mockMvc.perform(get("/actuator/info"))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Should allow access to swagger UI without authentication")
                void shouldAllowAccessToSwaggerUI() throws Exception {
                        mockMvc.perform(get("/swagger-ui/index.html"))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Should allow access to API docs without authentication")
                void shouldAllowAccessToApiDocs() throws Exception {
                        mockMvc.perform(get("/v3/api-docs"))
                                        .andExpect(status().isOk());
                }
        }

        @Nested
        @DisplayName("Protected Endpoint Access Tests")
        class ProtectedEndpointAccessTests {

                @Test
                @DisplayName("Should deny access to protected endpoints without token")
                void shouldDenyAccessToProtectedEndpointsWithoutToken() throws Exception {
                        mockMvc.perform(get("/api/v1/exceptions"))
                                        .andExpect(status().isUnauthorized());

                        mockMvc.perform(post("/api/v1/exceptions/test-id/retry"))
                                        .andExpect(status().isUnauthorized());

                        mockMvc.perform(put("/api/v1/exceptions/test-id/acknowledge"))
                                        .andExpect(status().isUnauthorized());

                        mockMvc.perform(put("/api/v1/exceptions/test-id/resolve"))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("Should allow access with valid JWT token")
                void shouldAllowAccessWithValidJwtToken() throws Exception {
                        String token = createValidJwtToken("testuser", List.of("OPERATOR"));

                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + token))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Should deny access with invalid JWT token")
                void shouldDenyAccessWithInvalidJwtToken() throws Exception {
                        String invalidToken = "invalid.jwt.token";

                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + invalidToken))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("Should deny access with expired JWT token")
                void shouldDenyAccessWithExpiredJwtToken() throws Exception {
                        String expiredToken = createExpiredJwtToken("testuser", List.of("OPERATOR"));

                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + expiredToken))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("Should deny access with malformed Authorization header")
                void shouldDenyAccessWithMalformedAuthorizationHeader() throws Exception {
                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                                        .andExpect(status().isUnauthorized());

                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "InvalidFormat"))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("Should deny access with empty Bearer token")
                void shouldDenyAccessWithEmptyBearerToken() throws Exception {
                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer "))
                                        .andExpect(status().isUnauthorized());
                }
        }

        @Nested
        @DisplayName("Role-Based Access Control Tests")
        class RoleBasedAccessControlTests {

                @Test
                @DisplayName("Should allow VIEWER role to read exceptions")
                void shouldAllowViewerRoleToReadExceptions() throws Exception {
                        String viewerToken = createValidJwtToken("viewer", List.of("VIEWER"));

                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + viewerToken))
                                        .andExpect(status().isOk());

                        mockMvc.perform(get("/api/v1/exceptions/test-id")
                                        .header("Authorization", "Bearer " + viewerToken))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Should deny VIEWER role from modifying exceptions")
                void shouldDenyViewerRoleFromModifyingExceptions() throws Exception {
                        String viewerToken = createValidJwtToken("viewer", List.of("VIEWER"));

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
                @DisplayName("Should allow OPERATIONS role to read and modify exceptions")
                void shouldAllowOperationsRoleToReadAndModifyExceptions() throws Exception {
                        String operatorToken = createValidJwtToken("operator", List.of("OPERATIONS"));

                        // Should be able to read
                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + operatorToken))
                                        .andExpect(status().isOk());

                        // Should be able to modify (these might return different status codes based on
                        // implementation)
                        mockMvc.perform(post("/api/v1/exceptions/test-id/retry")
                                        .header("Authorization", "Bearer " + operatorToken)
                                        .contentType("application/json")
                                        .content("{}"))
                                        .andExpect(status().isNotFound()); // Assuming test-id doesn't exist

                        mockMvc.perform(put("/api/v1/exceptions/test-id/acknowledge")
                                        .header("Authorization", "Bearer " + operatorToken)
                                        .contentType("application/json")
                                        .content("{}"))
                                        .andExpect(status().isNotFound()); // Assuming test-id doesn't exist
                }

                @Test
                @DisplayName("Should allow ADMIN role to access all endpoints")
                void shouldAllowAdminRoleToAccessAllEndpoints() throws Exception {
                        String adminToken = createValidJwtToken("admin", List.of("ADMIN"));

                        // Should be able to read exceptions
                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + adminToken))
                                        .andExpect(status().isOk());

                        // Should be able to access admin endpoints
                        mockMvc.perform(get("/actuator/metrics")
                                        .header("Authorization", "Bearer " + adminToken))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Should deny non-admin roles from accessing admin endpoints")
                void shouldDenyNonAdminRolesFromAccessingAdminEndpoints() throws Exception {
                        String operatorToken = createValidJwtToken("operator", List.of("OPERATIONS"));
                        String viewerToken = createValidJwtToken("viewer", List.of("VIEWER"));

                        mockMvc.perform(get("/actuator/metrics")
                                        .header("Authorization", "Bearer " + operatorToken))
                                        .andExpect(status().isForbidden());

                        mockMvc.perform(get("/actuator/metrics")
                                        .header("Authorization", "Bearer " + viewerToken))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("Should handle multiple roles correctly")
                void shouldHandleMultipleRolesCorrectly() throws Exception {
                        String multiRoleToken = createValidJwtToken("multiuser",
                                        List.of("VIEWER", "OPERATIONS", "ADMIN"));

                        // Should be able to access all endpoints
                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + multiRoleToken))
                                        .andExpect(status().isOk());

                        mockMvc.perform(get("/actuator/metrics")
                                        .header("Authorization", "Bearer " + multiRoleToken))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Should deny access for users with no roles")
                void shouldDenyAccessForUsersWithNoRoles() throws Exception {
                        String noRoleToken = createValidJwtToken("noroleuser", List.of());

                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + noRoleToken))
                                        .andExpect(status().isForbidden());
                }
        }

        @Nested
        @DisplayName("HTTP Method Security Tests")
        class HttpMethodSecurityTests {

                @Test
                @DisplayName("Should enforce method-level security for GET requests")
                void shouldEnforceMethodLevelSecurityForGetRequests() throws Exception {
                        String viewerToken = createValidJwtToken("viewer", List.of("VIEWER"));
                        String operatorToken = createValidJwtToken("operator", List.of("OPERATIONS"));

                        // Both VIEWER and OPERATIONS should be able to GET
                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + viewerToken))
                                        .andExpect(status().isOk());

                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + operatorToken))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Should enforce method-level security for POST requests")
                void shouldEnforceMethodLevelSecurityForPostRequests() throws Exception {
                        String viewerToken = createValidJwtToken("viewer", List.of("VIEWER"));
                        String operatorToken = createValidJwtToken("operator", List.of("OPERATIONS"));

                        // VIEWER should be denied POST
                        mockMvc.perform(post("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + viewerToken)
                                        .contentType("application/json")
                                        .content("{}"))
                                        .andExpect(status().isForbidden());

                        // OPERATIONS should be allowed POST (might return different status based on
                        // implementation)
                        mockMvc.perform(post("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + operatorToken)
                                        .contentType("application/json")
                                        .content("{}"))
                                        .andExpect(status().isBadRequest()); // Assuming invalid request body
                }

                @Test
                @DisplayName("Should enforce method-level security for PUT requests")
                void shouldEnforceMethodLevelSecurityForPutRequests() throws Exception {
                        String viewerToken = createValidJwtToken("viewer", List.of("VIEWER"));
                        String operatorToken = createValidJwtToken("operator", List.of("OPERATIONS"));

                        // VIEWER should be denied PUT
                        mockMvc.perform(put("/api/v1/exceptions/test-id/acknowledge")
                                        .header("Authorization", "Bearer " + viewerToken)
                                        .contentType("application/json")
                                        .content("{}"))
                                        .andExpect(status().isForbidden());

                        // OPERATIONS should be allowed PUT (might return different status based on
                        // implementation)
                        mockMvc.perform(put("/api/v1/exceptions/test-id/acknowledge")
                                        .header("Authorization", "Bearer " + operatorToken)
                                        .contentType("application/json")
                                        .content("{}"))
                                        .andExpect(status().isNotFound()); // Assuming test-id doesn't exist
                }
        }

        @Nested
        @DisplayName("Security Configuration Tests")
        class SecurityConfigurationTests {

                @Test
                @DisplayName("Should have CSRF disabled for stateless API")
                void shouldHaveCsrfDisabledForStatelessApi() throws Exception {
                        String operatorToken = createValidJwtToken("operator", List.of("OPERATIONS"));

                        // POST request should work without CSRF token
                        mockMvc.perform(post("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + operatorToken)
                                        .contentType("application/json")
                                        .content("{}"))
                                        .andExpect(status().isBadRequest()); // Should not be 403 due to CSRF
                }

                @Test
                @DisplayName("Should use stateless session management")
                void shouldUseStatelessSessionManagement() throws Exception {
                        String operatorToken = createValidJwtToken("operator", List.of("OPERATIONS"));

                        // Multiple requests should not create or use sessions
                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + operatorToken))
                                        .andExpect(status().isOk())
                                        .andExpect(header().doesNotExist("Set-Cookie"));

                        mockMvc.perform(get("/api/v1/exceptions")
                                        .header("Authorization", "Bearer " + operatorToken))
                                        .andExpect(status().isOk())
                                        .andExpect(header().doesNotExist("Set-Cookie"));
                }
        }

        private String createValidJwtToken(String username, List<String> roles) {
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                return Jwts.builder()
                                .subject(username)
                                .claim("roles", roles)
                                .issuedAt(new Date())
                                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                                .signWith(key)
                                .compact();
        }

        private String createExpiredJwtToken(String username, List<String> roles) {
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                return Jwts.builder()
                                .subject(username)
                                .claim("roles", roles)
                                .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                                .signWith(key)
                                .compact();
        }
}