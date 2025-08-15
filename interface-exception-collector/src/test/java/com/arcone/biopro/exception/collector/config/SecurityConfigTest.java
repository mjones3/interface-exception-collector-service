package com.arcone.biopro.exception.collector.config;

import com.arcone.biopro.exception.collector.config.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security configuration tests
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    private final String jwtSecret = "mySecretKey1234567890123456789012345678901234567890";

    @Test
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToProtectedEndpointsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/exceptions"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/exceptions/test-id/retry"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessWithValidJwtToken() throws Exception {
        String token = createValidJwtToken("testuser", List.of("OPERATOR"));

        mockMvc.perform(get("/api/v1/exceptions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessWithInvalidJwtToken() throws Exception {
        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(get("/api/v1/exceptions")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithExpiredJwtToken() throws Exception {
        String expiredToken = createExpiredJwtToken("testuser", List.of("OPERATOR"));

        mockMvc.perform(get("/api/v1/exceptions")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldEnforceRoleBasedAccess() throws Exception {
        // VIEWER role should be able to read but not modify
        String viewerToken = createValidJwtToken("viewer", List.of("VIEWER"));

        mockMvc.perform(get("/api/v1/exceptions")
                .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/exceptions/test-id/retry")
                .header("Authorization", "Bearer " + viewerToken)
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden());

        // OPERATOR role should be able to read and modify
        String operatorToken = createValidJwtToken("operator", List.of("OPERATOR"));

        mockMvc.perform(get("/api/v1/exceptions")
                .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToAdminEndpointsForNonAdminUsers() throws Exception {
        String operatorToken = createValidJwtToken("operator", List.of("OPERATOR"));

        mockMvc.perform(get("/actuator/metrics")
                .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isForbidden());

        String adminToken = createValidJwtToken("admin", List.of("ADMIN"));

        mockMvc.perform(get("/actuator/metrics")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
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