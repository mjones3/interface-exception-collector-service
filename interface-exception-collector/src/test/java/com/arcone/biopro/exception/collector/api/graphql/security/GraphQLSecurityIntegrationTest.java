package com.arcone.biopro.exception.collector.api.graphql.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GraphQL security features including rate limiting,
 * query allowlist, and audit logging.
 * 
 * DISABLED: This test class depends on Redis for rate limiting, but Redis is
 * disabled in this project.
 */
@Disabled("Redis is disabled - rate limiting tests cannot run")
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class GraphQLSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldAllowValidGraphQLQueryWithinRateLimit() throws Exception {
        // Given - rate limiting is disabled since Redis is not available

        String query = """
                {
                    "query": "query { __typename }"
                }
                """;

        // When & Then
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(query))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldRejectQueryWhenRateLimitExceeded() throws Exception {
        // Given - rate limiting is disabled since Redis is not available

        String query = """
                {
                    "query": "query { __typename }"
                }
                """;

        // When & Then
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(query))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].extensions.errorCode").value("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowHigherRateLimitsForAdminUsers() throws Exception {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(250L); // Would exceed VIEWER limit but not ADMIN

        String query = """
                {
                    "query": "query { __typename }"
                }
                """;

        // When & Then
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(query))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectUnauthenticatedRequests() throws Exception {
        // Given
        String query = """
                {
                    "query": "query { __typename }"
                }
                """;

        // When & Then
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(query))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldIncludeCorsHeaders() throws Exception {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(30L);

        String query = """
                {
                    "query": "query { __typename }"
                }
                """;

        // When & Then
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(query)
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldIncludeSecurityHeaders() throws Exception {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(30L);

        String query = """
                {
                    "query": "query { __typename }"
                }
                """;

        // When & Then
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(query))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldHandleComplexQuery() throws Exception {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(30L);

        // Very simple query that should be allowed
        String query = """
                {
                    "query": "query { __typename }"
                }
                """;

        // When & Then
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(query))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldRejectExcessivelyComplexQuery() throws Exception {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(30L);

        // Create a very long query that exceeds complexity limits
        StringBuilder complexQuery = new StringBuilder("query { ");
        for (int i = 0; i < 2000; i++) {
            complexQuery.append("field").append(i).append(" ");
        }
        complexQuery.append("}");

        String query = String.format("""
                {
                    "query": "%s"
                }
                """, complexQuery.toString());

        // When & Then
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(query))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].extensions.errorCode").value("RATE_LIMIT_EXCEEDED"));
    }
}