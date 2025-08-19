package com.arcone.biopro.exception.collector.api.graphql.security;

import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLSecurityService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GraphQL security components including authentication,
 * authorization, rate limiting, and audit logging.
 */
@ExtendWith(MockitoExtension.class)
class GraphQLSecurityUnitTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityAuditLogger auditLogger;

    @InjectMocks
    private GraphQLSecurityService securityService;

    @InjectMocks
    private RateLimitingInterceptor rateLimitingInterceptor;

    private InterfaceException testException;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        testException = InterfaceException.builder()
                .id(1L)
                .transactionId("TXN-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.HIGH)
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .build();
    }

    @Test
    void canViewPayload_WithAdminRole_ShouldReturnTrue() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When
        boolean result = securityService.canViewPayload(testException, authentication);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canViewPayload_WithOperationsRole_ShouldReturnTrue() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // When
        boolean result = securityService.canViewPayload(testException, authentication);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canViewPayload_WithViewerRole_ShouldReturnFalse() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );

        // When
        boolean result = securityService.canViewPayload(testException, authentication);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canViewPayload_WithNoRoles_ShouldReturnFalse() {
        // Given
        when(authentication.getAuthorities()).thenReturn(List.of());

        // When
        boolean result = securityService.canViewPayload(testException, authentication);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canPerformRetry_WithOperationsRole_ShouldReturnTrue() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // When
        boolean result = securityService.canPerformRetry(testException, authentication);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformRetry_WithAdminRole_ShouldReturnTrue() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When
        boolean result = securityService.canPerformRetry(testException, authentication);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canPerformRetry_WithViewerRole_ShouldReturnFalse() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );

        // When
        boolean result = securityService.canPerformRetry(testException, authentication);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canPerformRetry_WithNonRetryableException_ShouldReturnFalse() {
        // Given
        testException.setRetryable(false);
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // When
        boolean result = securityService.canPerformRetry(testException, authentication);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canAcknowledgeException_WithOperationsRole_ShouldReturnTrue() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // When
        boolean result = securityService.canAcknowledgeException(testException, authentication);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canAcknowledgeException_WithAlreadyAcknowledged_ShouldReturnFalse() {
        // Given
        testException.setStatus(ExceptionStatus.ACKNOWLEDGED);
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // When
        boolean result = securityService.canAcknowledgeException(testException, authentication);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isRateLimitExceeded_WithinLimit_ShouldReturnFalse() {
        // Given
        String userId = "test-user";
        when(valueOperations.get("rate_limit:user:" + userId)).thenReturn(30L);

        // When
        boolean result = rateLimitingInterceptor.isRateLimitExceeded(userId, "VIEWER");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isRateLimitExceeded_ExceedsLimit_ShouldReturnTrue() {
        // Given
        String userId = "test-user";
        when(valueOperations.get("rate_limit:user:" + userId)).thenReturn(100L);

        // When
        boolean result = rateLimitingInterceptor.isRateLimitExceeded(userId, "VIEWER");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isRateLimitExceeded_AdminRole_ShouldHaveHigherLimit() {
        // Given
        String userId = "admin-user";
        when(valueOperations.get("rate_limit:user:" + userId)).thenReturn(200L);

        // When
        boolean result = rateLimitingInterceptor.isRateLimitExceeded(userId, "ADMIN");

        // Then
        assertThat(result).isFalse(); // Admin limit is higher (500)
    }

    @Test
    void isRateLimitExceeded_OperationsRole_ShouldHaveModerateLimit() {
        // Given
        String userId = "ops-user";
        when(valueOperations.get("rate_limit:user:" + userId)).thenReturn(150L);

        // When
        boolean result = rateLimitingInterceptor.isRateLimitExceeded(userId, "OPERATIONS");

        // Then
        assertThat(result).isFalse(); // Operations limit is 200
    }

    @Test
    void incrementRateLimit_ShouldIncrementAndSetExpiry() {
        // Given
        String userId = "test-user";
        when(valueOperations.get("rate_limit:user:" + userId)).thenReturn(null);

        // When
        rateLimitingInterceptor.incrementRateLimit(userId);

        // Then
        verify(valueOperations).set(eq("rate_limit:user:" + userId), eq(1L), eq(Duration.ofHours(1)));
    }

    @Test
    void incrementRateLimit_WithExistingCount_ShouldIncrement() {
        // Given
        String userId = "test-user";
        when(valueOperations.get("rate_limit:user:" + userId)).thenReturn(5L);

        // When
        rateLimitingInterceptor.incrementRateLimit(userId);

        // Then
        verify(valueOperations).set(eq("rate_limit:user:" + userId), eq(6L), eq(Duration.ofHours(1)));
    }

    @Test
    void hasRole_WithMatchingRole_ShouldReturnTrue() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(
                        new SimpleGrantedAuthority("ROLE_VIEWER"),
                        new SimpleGrantedAuthority("ROLE_OPERATIONS")
                )
        );

        // When
        boolean result = securityService.hasRole(authentication, "OPERATIONS");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasRole_WithoutMatchingRole_ShouldReturnFalse() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
        );

        // When
        boolean result = securityService.hasRole(authentication, "ADMIN");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_WithMatchingRole_ShouldReturnTrue() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // When
        boolean result = securityService.hasAnyRole(authentication, "ADMIN", "OPERATIONS", "VIEWER");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasAnyRole_WithoutMatchingRole_ShouldReturnFalse() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_GUEST"))
        );

        // When
        boolean result = securityService.hasAnyRole(authentication, "ADMIN", "OPERATIONS", "VIEWER");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canViewCustomerData_WithCustomerMatch_ShouldReturnTrue() {
        // Given
        when(authentication.getName()).thenReturn("user@customer-001.com");

        // When
        boolean result = securityService.canViewCustomerData(testException, authentication);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canViewCustomerData_WithoutCustomerMatch_ShouldReturnFalse() {
        // Given
        when(authentication.getName()).thenReturn("user@customer-002.com");

        // When
        boolean result = securityService.canViewCustomerData(testException, authentication);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canViewCustomerData_WithAdminRole_ShouldReturnTrue() {
        // Given
        when(authentication.getName()).thenReturn("admin@company.com");
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When
        boolean result = securityService.canViewCustomerData(testException, authentication);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void getUserId_ShouldExtractFromAuthentication() {
        // Given
        when(authentication.getName()).thenReturn("test-user");

        // When
        String result = securityService.getUserId(authentication);

        // Then
        assertThat(result).isEqualTo("test-user");
    }

    @Test
    void getUserRole_ShouldExtractHighestRole() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(
                        new SimpleGrantedAuthority("ROLE_VIEWER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );

        // When
        String result = securityService.getUserRole(authentication);

        // Then
        assertThat(result).isEqualTo("ADMIN"); // Highest role
    }

    @Test
    void getUserRole_WithSingleRole_ShouldReturnThatRole() {
        // Given
        when(authentication.getAuthorities()).thenReturn(
                List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS"))
        );

        // When
        String result = securityService.getUserRole(authentication);

        // Then
        assertThat(result).isEqualTo("OPERATIONS");
    }

    @Test
    void getUserRole_WithNoRoles_ShouldReturnGuest() {
        // Given
        when(authentication.getAuthorities()).thenReturn(List.of());

        // When
        String result = securityService.getUserRole(authentication);

        // Then
        assertThat(result).isEqualTo("GUEST");
    }
}