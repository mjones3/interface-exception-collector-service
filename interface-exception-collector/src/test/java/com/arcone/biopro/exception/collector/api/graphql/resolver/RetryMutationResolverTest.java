package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.*;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLRetryService;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLAcknowledgmentService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for RetryMutationResolver.
 */
@ExtendWith(MockitoExtension.class)
class RetryMutationResolverTest {

        @Mock
        private GraphQLRetryService graphQLRetryService;

        @Mock
        private GraphQLAcknowledgmentService graphQLAcknowledgmentService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private RetryMutationResolver retryMutationResolver;

        private RetryExceptionInput retryInput;
        private BulkRetryInput bulkRetryInput;
        private AcknowledgeExceptionInput acknowledgeInput;
        private BulkAcknowledgeInput bulkAcknowledgeInput;
        private InterfaceException testException;
        private RetryAttempt testRetryAttempt;

        @BeforeEach
        void setUp() {
                // Setup test data
                retryInput = RetryExceptionInput.builder()
                                .transactionId("test-transaction-123")
                                .reason("Manual retry requested")
                                .priority(RetryExceptionInput.RetryPriority.NORMAL)
                                .notes("Test retry")
                                .build();

                bulkRetryInput = BulkRetryInput.builder()
                                .transactionIds(List.of("test-transaction-123", "test-transaction-456"))
                                .reason("Bulk retry requested")
                                .priority(RetryExceptionInput.RetryPriority.HIGH)
                                .build();

                acknowledgeInput = AcknowledgeExceptionInput.builder()
                                .transactionId("test-transaction-123")
                                .reason("Manual acknowledgment requested")
                                .notes("Test acknowledgment")
                                .assignedTo("test-user")
                                .build();

                bulkAcknowledgeInput = BulkAcknowledgeInput.builder()
                                .transactionIds(List.of("test-transaction-123", "test-transaction-456"))
                                .reason("Bulk acknowledgment requested")
                                .notes("Test bulk acknowledgment")
                                .assignedTo("test-user")
                                .build();

                testException = InterfaceException.builder()
                                .id(1L)
                                .transactionId("test-transaction-123")
                                .interfaceType(InterfaceType.ORDER)
                                .exceptionReason("Test exception")
                                .operation("CREATE_ORDER")
                                .status(ExceptionStatus.NEW)
                                .retryable(true)
                                .retryCount(0)
                                .maxRetries(3)
                                .timestamp(OffsetDateTime.now())
                                .processedAt(OffsetDateTime.now())
                                .build();

                testRetryAttempt = RetryAttempt.builder()
                                .id(1L)
                                .interfaceException(testException)
                                .attemptNumber(1)
                                .status(RetryStatus.PENDING)
                                .initiatedBy("test-user")
                                .initiatedAt(OffsetDateTime.now())
                                .build();

                // Setup authentication mock
                when(authentication.getName()).thenReturn("test-user");
                lenient().when(authentication.getAuthorities()).thenReturn(
                                (java.util.Collection) List.of(new SimpleGrantedAuthority("ROLE_OPERATIONS")));
        }

        @Test
        void testRetryException_Success() {
                // Arrange
                RetryExceptionResult expectedResult = RetryExceptionResult.builder()
                                .success(true)
                                .exception(testException)
                                .retryAttempt(testRetryAttempt)
                                .errors(List.of())
                                .build();

                when(graphQLRetryService.retryException(any(RetryExceptionInput.class), any(Authentication.class)))
                                .thenReturn(CompletableFuture.completedFuture(expectedResult));

                // Act
                CompletableFuture<RetryExceptionResult> result = retryMutationResolver.retryException(retryInput,
                                authentication);

                // Assert
                assertNotNull(result);
                RetryExceptionResult actualResult = result.join();
                assertTrue(actualResult.isSuccess());
                assertEquals(testException, actualResult.getException());
                assertEquals(testRetryAttempt, actualResult.getRetryAttempt());
                assertTrue(actualResult.getErrors().isEmpty());

                verify(graphQLRetryService).retryException(eq(retryInput), eq(authentication));
        }

        @Test
        void testRetryException_ServiceException() {
                // Arrange
                when(graphQLRetryService.retryException(any(RetryExceptionInput.class), any(Authentication.class)))
                                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service error")));

                // Act
                CompletableFuture<RetryExceptionResult> result = retryMutationResolver.retryException(retryInput,
                                authentication);

                // Assert
                assertNotNull(result);
                RetryExceptionResult actualResult = result.join();
                assertFalse(actualResult.isSuccess());
                assertNull(actualResult.getException());
                assertNull(actualResult.getRetryAttempt());
                assertFalse(actualResult.getErrors().isEmpty());
                assertEquals("RETRY_OPERATION_FAILED", actualResult.getErrors().get(0).getCode());
        }

        @Test
        void testBulkRetryExceptions_Success() {
                // Arrange
                BulkRetryResult expectedResult = BulkRetryResult.builder()
                                .successCount(2)
                                .failureCount(0)
                                .results(List.of(
                                                RetryExceptionResult.builder().success(true).build(),
                                                RetryExceptionResult.builder().success(true).build()))
                                .errors(List.of())
                                .build();

                when(graphQLRetryService.bulkRetryExceptions(any(BulkRetryInput.class), any(Authentication.class)))
                                .thenReturn(CompletableFuture.completedFuture(expectedResult));

                // Act
                CompletableFuture<BulkRetryResult> result = retryMutationResolver.bulkRetryExceptions(bulkRetryInput,
                                authentication);

                // Assert
                assertNotNull(result);
                BulkRetryResult actualResult = result.join();
                assertEquals(2, actualResult.getSuccessCount());
                assertEquals(0, actualResult.getFailureCount());
                assertEquals(2, actualResult.getResults().size());
                assertTrue(actualResult.getErrors().isEmpty());

                verify(graphQLRetryService).bulkRetryExceptions(eq(bulkRetryInput), eq(authentication));
        }

        @Test
        void testBulkRetryExceptions_ServiceException() {
                // Arrange
                when(graphQLRetryService.bulkRetryExceptions(any(BulkRetryInput.class), any(Authentication.class)))
                                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Bulk service error")));

                // Act
                CompletableFuture<BulkRetryResult> result = retryMutationResolver.bulkRetryExceptions(bulkRetryInput,
                                authentication);

                // Assert
                assertNotNull(result);
                BulkRetryResult actualResult = result.join();
                assertEquals(0, actualResult.getSuccessCount());
                assertEquals(2, actualResult.getFailureCount());
                assertTrue(actualResult.getResults().isEmpty());
                assertFalse(actualResult.getErrors().isEmpty());
                assertEquals("BULK_RETRY_OPERATION_FAILED", actualResult.getErrors().get(0).getCode());
        }

        @Test
        void testCancelRetry_Success() {
                // Arrange
                CancelRetryResult expectedResult = CancelRetryResult.builder()
                                .success(true)
                                .exception(testException)
                                .errors(List.of())
                                .build();

                when(graphQLRetryService.cancelRetry(anyString(), anyString(), any(Authentication.class)))
                                .thenReturn(CompletableFuture.completedFuture(expectedResult));

                // Act
                CompletableFuture<CancelRetryResult> result = retryMutationResolver.cancelRetry(
                                "test-transaction-123", "User requested cancellation", authentication);

                // Assert
                assertNotNull(result);
                CancelRetryResult actualResult = result.join();
                assertTrue(actualResult.isSuccess());
                assertEquals(testException, actualResult.getException());
                assertTrue(actualResult.getErrors().isEmpty());

                verify(graphQLRetryService).cancelRetry(eq("test-transaction-123"), eq("User requested cancellation"),
                                eq(authentication));
        }

        @Test
        void testCancelRetry_ServiceException() {
                // Arrange
                when(graphQLRetryService.cancelRetry(anyString(), anyString(), any(Authentication.class)))
                                .thenReturn(CompletableFuture
                                                .failedFuture(new RuntimeException("Cancel service error")));

                // Act
                CompletableFuture<CancelRetryResult> result = retryMutationResolver.cancelRetry(
                                "test-transaction-123", "User requested cancellation", authentication);

                // Assert
                assertNotNull(result);
                CancelRetryResult actualResult = result.join();
                assertFalse(actualResult.isSuccess());
                assertNull(actualResult.getException());
                assertFalse(actualResult.getErrors().isEmpty());
                assertEquals("CANCEL_RETRY_OPERATION_FAILED", actualResult.getErrors().get(0).getCode());
        }

        @Test
        void testAcknowledgeException_Success() {
                // Arrange
                InterfaceException acknowledgedException = InterfaceException.builder()
                                .id(1L)
                                .transactionId("test-transaction-123")
                                .interfaceType(InterfaceType.ORDER)
                                .exceptionReason("Test exception")
                                .operation("CREATE_ORDER")
                                .status(ExceptionStatus.ACKNOWLEDGED)
                                .acknowledgedBy("test-user")
                                .acknowledgedAt(OffsetDateTime.now())
                                .retryable(true)
                                .retryCount(0)
                                .maxRetries(3)
                                .timestamp(OffsetDateTime.now())
                                .processedAt(OffsetDateTime.now())
                                .build();

                AcknowledgeExceptionResult expectedResult = AcknowledgeExceptionResult.builder()
                                .success(true)
                                .exception(acknowledgedException)
                                .errors(List.of())
                                .build();

                when(graphQLAcknowledgmentService.acknowledgeException(any(AcknowledgeExceptionInput.class),
                                any(Authentication.class)))
                                .thenReturn(CompletableFuture.completedFuture(expectedResult));

                // Act
                CompletableFuture<AcknowledgeExceptionResult> result = retryMutationResolver.acknowledgeException(
                                acknowledgeInput,
                                authentication);

                // Assert
                assertNotNull(result);
                AcknowledgeExceptionResult actualResult = result.join();
                assertTrue(actualResult.isSuccess());
                assertEquals(acknowledgedException, actualResult.getException());
                assertTrue(actualResult.getErrors().isEmpty());

                verify(graphQLAcknowledgmentService).acknowledgeException(eq(acknowledgeInput), eq(authentication));
        }

        @Test
        void testAcknowledgeException_ServiceException() {
                // Arrange
                when(graphQLAcknowledgmentService.acknowledgeException(any(AcknowledgeExceptionInput.class),
                                any(Authentication.class)))
                                .thenReturn(CompletableFuture
                                                .failedFuture(new RuntimeException("Acknowledgment service error")));

                // Act
                CompletableFuture<AcknowledgeExceptionResult> result = retryMutationResolver.acknowledgeException(
                                acknowledgeInput,
                                authentication);

                // Assert
                assertNotNull(result);
                AcknowledgeExceptionResult actualResult = result.join();
                assertFalse(actualResult.isSuccess());
                assertNull(actualResult.getException());
                assertFalse(actualResult.getErrors().isEmpty());
                assertEquals("ACKNOWLEDGMENT_OPERATION_FAILED", actualResult.getErrors().get(0).getCode());
        }

        @Test
        void testBulkAcknowledgeExceptions_Success() {
                // Arrange
                BulkAcknowledgeResult expectedResult = BulkAcknowledgeResult.builder()
                                .successCount(2)
                                .failureCount(0)
                                .results(List.of(
                                                AcknowledgeExceptionResult.builder().success(true).build(),
                                                AcknowledgeExceptionResult.builder().success(true).build()))
                                .errors(List.of())
                                .build();

                when(graphQLAcknowledgmentService.bulkAcknowledgeExceptions(any(BulkAcknowledgeInput.class),
                                any(Authentication.class)))
                                .thenReturn(CompletableFuture.completedFuture(expectedResult));

                // Act
                CompletableFuture<BulkAcknowledgeResult> result = retryMutationResolver.bulkAcknowledgeExceptions(
                                bulkAcknowledgeInput,
                                authentication);

                // Assert
                assertNotNull(result);
                BulkAcknowledgeResult actualResult = result.join();
                assertEquals(2, actualResult.getSuccessCount());
                assertEquals(0, actualResult.getFailureCount());
                assertEquals(2, actualResult.getResults().size());
                assertTrue(actualResult.getErrors().isEmpty());

                verify(graphQLAcknowledgmentService).bulkAcknowledgeExceptions(eq(bulkAcknowledgeInput),
                                eq(authentication));
        }

        @Test
        void testBulkAcknowledgeExceptions_ServiceException() {
                // Arrange
                when(graphQLAcknowledgmentService.bulkAcknowledgeExceptions(any(BulkAcknowledgeInput.class),
                                any(Authentication.class)))
                                .thenReturn(CompletableFuture.failedFuture(
                                                new RuntimeException("Bulk acknowledgment service error")));

                // Act
                CompletableFuture<BulkAcknowledgeResult> result = retryMutationResolver.bulkAcknowledgeExceptions(
                                bulkAcknowledgeInput,
                                authentication);

                // Assert
                assertNotNull(result);
                BulkAcknowledgeResult actualResult = result.join();
                assertEquals(0, actualResult.getSuccessCount());
                assertEquals(2, actualResult.getFailureCount());
                assertTrue(actualResult.getResults().isEmpty());
                assertFalse(actualResult.getErrors().isEmpty());
                assertEquals("BULK_ACKNOWLEDGMENT_OPERATION_FAILED", actualResult.getErrors().get(0).getCode());
        }
}