package com.arcone.biopro.exception.collector.api.graphql.config;

import com.arcone.biopro.exception.collector.api.graphql.dataloader.ExceptionDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.PayloadDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.RetryHistoryDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.StatusChangeDataLoader;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration for GraphQL unit tests.
 * Provides mock beans and test-specific configurations.
 */
@TestConfiguration
public class GraphQLTestConfiguration {

    @MockBean
    private InterfaceExceptionRepository exceptionRepository;

    @MockBean
    private RetryAttemptRepository retryAttemptRepository;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    @Primary
    public ExceptionDataLoader testExceptionDataLoader() {
        return new ExceptionDataLoader(exceptionRepository);
    }

    @Bean
    @Primary
    public RetryHistoryDataLoader testRetryHistoryDataLoader() {
        return new RetryHistoryDataLoader(retryAttemptRepository, exceptionRepository);
    }

    @Bean
    @Primary
    public PayloadDataLoader testPayloadDataLoader() {
        return mock(PayloadDataLoader.class);
    }

    @Bean
    @Primary
    public StatusChangeDataLoader testStatusChangeDataLoader() {
        return mock(StatusChangeDataLoader.class);
    }

    @Bean
    @Primary
    public DataLoader<String, com.arcone.biopro.exception.collector.domain.entity.InterfaceException> testExceptionLoader() {
        DataLoaderOptions options = DataLoaderOptions.newOptions()
                .setCachingEnabled(true)
                .setBatchingEnabled(true)
                .build();
        return DataLoader.newDataLoader(testExceptionDataLoader(), options);
    }

    @Bean
    @Primary
    public DataLoader<String, java.util.List<com.arcone.biopro.exception.collector.domain.entity.RetryAttempt>> testRetryHistoryLoader() {
        DataLoaderOptions options = DataLoaderOptions.newOptions()
                .setCachingEnabled(true)
                .setBatchingEnabled(true)
                .build();
        return DataLoader.newDataLoader(testRetryHistoryDataLoader(), options);
    }

    /**
     * Sets up a mock security context for testing.
     */
    public static void setupMockSecurityContext(String username, String... roles) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        when(authentication.isAuthenticated()).thenReturn(true);

        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                .collect(java.util.stream.Collectors.toList());
        when(authentication.getAuthorities()).thenReturn(authorities);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Clears the security context after testing.
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}