package com.arcone.biopro.exception.collector.infrastructure.config.security;

import com.arcone.biopro.exception.collector.infrastructure.config.GraphQLSecurityConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GraphQL Security Configuration.
 * Tests JWT authentication, role-based access control, and GraphQL endpoint
 * security.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class GraphQLSecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private SecretKey jwtSecretKey;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Use the same secret as configured in application-test.yml
        String secret = "mySecretKey1234567890123456789012345678901234567890";
        jwtSecretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        // Health endpoint should be accessible without authentication
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        // Info endpoint should be accessible without authentication
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToGraphQLWithoutAuthentication() throws Exception {
        // GraphQL endpoint should require authentication
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\": \"{ __typename }\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToGraphQLWithValidJWT() throws Exception {
        String token = createJwtToken("testuser", List.of("VIEWER"));

        mockMvc.perform(post("/graphql")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\": \"{ __typename }\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessWithExpiredJWT() throws Exception {
        String expiredToken = createExpiredJwtToken("testuser", List.of("VIEWER"));

        mockMvc.perform(post("/graphql")
                .header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\": \"{ __typename }\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithInvalidJWT() throws Exception {
        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(post("/graphql")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\": \"{ __typename }\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminAccessToActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldDenyViewerAccessToActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAccessToGraphiQLInDevelopment() throws Exception {
        // GraphiQL should be accessible without authentication in development
        mockMvc.perform(get("/graphiql"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandleCORSForGraphQLEndpoints() throws Exception {
        String token = createJwtToken("testuser", List.of("VIEWER"));

        mockMvc.perform(post("/graphql")
                .header("Authorization", "Bearer " + token)
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\": \"{ __typename }\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void shouldHandlePreflightCORSRequests() throws Exception {
        mockMvc.perform(options("/graphql")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    /**
     * Creates a valid JWT token for testing.
     */
    private String createJwtToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiration = now.plus(1, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(jwtSecretKey)
                .compact();
    }

    /**
     * Creates an expired JWT token for testing.
     */
    private String createExpiredJwtToken(String username, List<String> roles) {
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant expiration = past.plus(30, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(past))
                .expiration(Date.from(expiration))
                .signWith(jwtSecretKey)
                .compact();
    }
}