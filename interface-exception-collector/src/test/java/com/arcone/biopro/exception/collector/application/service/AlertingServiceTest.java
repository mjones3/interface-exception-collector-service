package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.kafka.publisher.AlertPublisher;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.support.SendResult;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AlertingService.
 * Tests all alerting rules and conditions as per requirement US-015.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlertingService Tests")
class AlertingServiceTest {

    @Mock
    private AlertPublisher alertPublisher;

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @InjectMocks
    private AlertingService alertingService;

    private InterfaceException baseException;
    private CompletableFuture<SendResult<String, Object>> mockFuture;

    @BeforeEach
    void setUp() {
        baseException = InterfaceException.builder()
                .id(1L)
                .transactionId("test-transaction-123")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception reason")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.BUSINESS_RULE)
                .retryable(true)
                .customerId("CUST001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryCount(0)
                .build();

        mockFuture = CompletableFuture.completedFuture(mock(SendResult.class));
    }

    @Nested
    @DisplayName("Critical Severity Alerting")
    class CriticalSeverityAlertingTests {

        @Test
        @DisplayName("Should generate critical severity alert for CRITICAL severity exception")
        void shouldGenerateCriticalSeverityAlert() {
            // Given
            baseException.setSeverity(ExceptionSeverity.CRITICAL);
            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    isNull(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> alertLevelCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> alertReasonCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> escalationTeamCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> estimatedImpactCaptor = ArgumentCaptor.forClass(String.class);

            verify(alertPublisher).publishCriticalAlert(
                    eq(1L),
                    eq("test-transaction-123"),
                    alertLevelCaptor.capture(),
                    alertReasonCaptor.capture(),
                    eq("ORDER"),
                    eq("Test exception reason"),
                    eq("CUST001"),
                    escalationTeamCaptor.capture(),
                    eq(true),
                    estimatedImpactCaptor.capture(),
                    isNull(),
                    anyString(),
                    anyString()
            );

            assertThat(alertLevelCaptor.getValue()).isEqualTo("CRITICAL");
            assertThat(alertReasonCaptor.getValue()).isEqualTo("CRITICAL_SEVERITY");
            assertThat(escalationTeamCaptor.getValue()).isEqualTo("OPERATIONS");
            assertThat(estimatedImpactCaptor.getValue()).isEqualTo("HIGH");
        }

        @Test
        @DisplayName("Should not generate alert for non-critical severity")
        void shouldNotGenerateAlertForNonCriticalSeverity() {
            // Given
            baseException.setSeverity(ExceptionSeverity.MEDIUM);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            verify(alertPublisher, never()).publishCriticalAlert(anyLong(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(),
                    anyString(), any(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should assign SEVERE impact for COLLECTION interface with CRITICAL severity")
        void shouldAssignSevereImpactForCollectionInterface() {
            // Given
            baseException.setSeverity(ExceptionSeverity.CRITICAL);
            baseException.setInterfaceType(InterfaceType.COLLECTION);
            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    isNull(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> estimatedImpactCaptor = ArgumentCaptor.forClass(String.class);
            verify(alertPublisher).publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(),
                    estimatedImpactCaptor.capture(), isNull(), anyString(), anyString());

            assertThat(estimatedImpactCaptor.getValue()).isEqualTo("SEVERE");
        }
    }

    @Nested
    @DisplayName("Multiple Retries Alerting")
    class MultipleRetriesAlertingTests {

        @Test
        @DisplayName("Should generate multiple retries alert when retry count exceeds threshold")
        void shouldGenerateMultipleRetriesAlert() {
            // Given
            baseException.setRetryCount(4); // Above threshold of 3
            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    isNull(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> alertLevelCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> alertReasonCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> estimatedImpactCaptor = ArgumentCaptor.forClass(String.class);

            verify(alertPublisher).publishCriticalAlert(
                    eq(1L),
                    eq("test-transaction-123"),
                    alertLevelCaptor.capture(),
                    alertReasonCaptor.capture(),
                    eq("ORDER"),
                    eq("Test exception reason"),
                    eq("CUST001"),
                    eq("OPERATIONS"),
                    eq(true),
                    estimatedImpactCaptor.capture(),
                    isNull(),
                    anyString(),
                    anyString()
            );

            assertThat(alertLevelCaptor.getValue()).isEqualTo("CRITICAL");
            assertThat(alertReasonCaptor.getValue()).isEqualTo("MULTIPLE_RETRIES_FAILED");
            assertThat(estimatedImpactCaptor.getValue()).isEqualTo("HIGH");
        }

        @Test
        @DisplayName("Should generate EMERGENCY alert for very high retry count")
        void shouldGenerateEmergencyAlertForVeryHighRetryCount() {
            // Given
            baseException.setRetryCount(6); // Above emergency threshold of 5
            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    isNull(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> alertLevelCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> escalationTeamCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> estimatedImpactCaptor = ArgumentCaptor.forClass(String.class);

            verify(alertPublisher).publishCriticalAlert(anyLong(), anyString(),
                    alertLevelCaptor.capture(), anyString(), anyString(), anyString(), anyString(),
                    escalationTeamCaptor.capture(), eq(true), estimatedImpactCaptor.capture(),
                    isNull(), anyString(), anyString());

            assertThat(alertLevelCaptor.getValue()).isEqualTo("EMERGENCY");
            assertThat(escalationTeamCaptor.getValue()).isEqualTo("MANAGEMENT");
            assertThat(estimatedImpactCaptor.getValue()).isEqualTo("SEVERE");
        }

        @Test
        @DisplayName("Should not generate alert for retry count within threshold")
        void shouldNotGenerateAlertForRetryCountWithinThreshold() {
            // Given
            baseException.setRetryCount(2); // Below threshold of 3

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            verify(alertPublisher, never()).publishCriticalAlert(anyLong(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(),
                    anyString(), any(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("System Error Alerting")
    class SystemErrorAlertingTests {

        @Test
        @DisplayName("Should generate system error alert for SYSTEM_ERROR category")
        void shouldGenerateSystemErrorAlert() {
            // Given
            baseException.setCategory(ExceptionCategory.SYSTEM_ERROR);
            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    isNull(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> alertLevelCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> alertReasonCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> escalationTeamCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> estimatedImpactCaptor = ArgumentCaptor.forClass(String.class);

            verify(alertPublisher).publishCriticalAlert(
                    eq(1L),
                    eq("test-transaction-123"),
                    alertLevelCaptor.capture(),
                    alertReasonCaptor.capture(),
                    eq("ORDER"),
                    eq("Test exception reason"),
                    eq("CUST001"),
                    escalationTeamCaptor.capture(),
                    eq(true),
                    estimatedImpactCaptor.capture(),
                    isNull(),
                    anyString(),
                    anyString()
            );

            assertThat(alertLevelCaptor.getValue()).isEqualTo("CRITICAL");
            assertThat(alertReasonCaptor.getValue()).isEqualTo("SYSTEM_ERROR");
            assertThat(escalationTeamCaptor.getValue()).isEqualTo("ENGINEERING");
            assertThat(estimatedImpactCaptor.getValue()).isEqualTo("SEVERE");
        }

        @Test
        @DisplayName("Should not generate alert for non-system error categories")
        void shouldNotGenerateAlertForNonSystemErrorCategories() {
            // Given
            baseException.setCategory(ExceptionCategory.BUSINESS_RULE);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            verify(alertPublisher, never()).publishCriticalAlert(anyLong(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(),
                    anyString(), any(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Customer Impact Alerting")
    class CustomerImpactAlertingTests {

        @Test
        @DisplayName("Should generate customer impact alert when threshold is exceeded")
        void shouldGenerateCustomerImpactAlert() {
            // Given
            baseException.setCustomerId("CUST001");
            when(exceptionRepository.countByTimestampBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                    .thenReturn(15L); // Above threshold of 10

            Page<InterfaceException> mockPage = new PageImpl<>(Collections.singletonList(baseException));
            when(exceptionRepository.findWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(mockPage);

            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    anyInt(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> alertReasonCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> escalationTeamCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Integer> customersAffectedCaptor = ArgumentCaptor.forClass(Integer.class);

            verify(alertPublisher).publishCriticalAlert(
                    eq(1L),
                    eq("test-transaction-123"),
                    eq("CRITICAL"),
                    alertReasonCaptor.capture(),
                    eq("ORDER"),
                    eq("Test exception reason"),
                    eq("CUST001"),
                    escalationTeamCaptor.capture(),
                    eq(true),
                    anyString(),
                    customersAffectedCaptor.capture(),
                    anyString(),
                    anyString()
            );

            assertThat(alertReasonCaptor.getValue()).isEqualTo("CUSTOMER_IMPACT");
            assertThat(escalationTeamCaptor.getValue()).isEqualTo("CUSTOMER_SUCCESS");
            assertThat(customersAffectedCaptor.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should generate EMERGENCY alert for severe customer impact")
        void shouldGenerateEmergencyAlertForSevereCustomerImpact() {
            // Given
            baseException.setCustomerId("CUST001");
            when(exceptionRepository.countByTimestampBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                    .thenReturn(60L); // Above severe threshold of 50

            // Create a list with 60 exceptions to simulate severe impact
            Page<InterfaceException> mockPage = new PageImpl<>(Collections.nCopies(60, baseException));
            when(exceptionRepository.findWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(mockPage);

            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    anyInt(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> alertLevelCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> escalationTeamCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> estimatedImpactCaptor = ArgumentCaptor.forClass(String.class);

            verify(alertPublisher).publishCriticalAlert(anyLong(), anyString(),
                    alertLevelCaptor.capture(), anyString(), anyString(), anyString(), anyString(),
                    escalationTeamCaptor.capture(), eq(true), estimatedImpactCaptor.capture(),
                    anyInt(), anyString(), anyString());

            assertThat(alertLevelCaptor.getValue()).isEqualTo("EMERGENCY");
            assertThat(escalationTeamCaptor.getValue()).isEqualTo("MANAGEMENT");
            assertThat(estimatedImpactCaptor.getValue()).isEqualTo("SEVERE");
        }

        @Test
        @DisplayName("Should not generate alert when customer ID is null")
        void shouldNotGenerateAlertWhenCustomerIdIsNull() {
            // Given
            baseException.setCustomerId(null);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            verify(exceptionRepository, never()).countByTimestampBetween(any(), any());
            verify(alertPublisher, never()).publishCriticalAlert(anyLong(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(),
                    anyString(), any(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should not generate alert when customer impact is below threshold")
        void shouldNotGenerateAlertWhenCustomerImpactBelowThreshold() {
            // Given
            baseException.setCustomerId("CUST001");
            when(exceptionRepository.countByTimestampBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                    .thenReturn(5L); // Below threshold of 10

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            verify(alertPublisher, never()).publishCriticalAlert(anyLong(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(),
                    anyString(), any(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Multiple Alert Conditions")
    class MultipleAlertConditionsTests {

        @Test
        @DisplayName("Should generate multiple alerts when multiple conditions are met")
        void shouldGenerateMultipleAlertsWhenMultipleConditionsMet() {
            // Given
            baseException.setSeverity(ExceptionSeverity.CRITICAL);
            baseException.setCategory(ExceptionCategory.SYSTEM_ERROR);
            baseException.setRetryCount(4);
            baseException.setCustomerId("CUST001");

            when(exceptionRepository.countByTimestampBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                    .thenReturn(15L);

            Page<InterfaceException> mockPage = new PageImpl<>(Collections.singletonList(baseException));
            when(exceptionRepository.findWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(mockPage);

            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    any(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            // Should generate 4 alerts: critical severity, multiple retries, system error, customer impact
            verify(alertPublisher, times(4)).publishCriticalAlert(anyLong(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(),
                    anyString(), any(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Escalation Team Assignment")
    class EscalationTeamAssignmentTests {

        @Test
        @DisplayName("Should assign MANAGEMENT team for EMERGENCY alerts")
        void shouldAssignManagementTeamForEmergencyAlerts() {
            // Given
            baseException.setRetryCount(6); // Triggers EMERGENCY level
            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    isNull(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> escalationTeamCaptor = ArgumentCaptor.forClass(String.class);
            verify(alertPublisher).publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), escalationTeamCaptor.capture(), anyBoolean(),
                    anyString(), isNull(), anyString(), anyString());

            assertThat(escalationTeamCaptor.getValue()).isEqualTo("MANAGEMENT");
        }

        @Test
        @DisplayName("Should assign ENGINEERING team for system errors")
        void shouldAssignEngineeringTeamForSystemErrors() {
            // Given
            baseException.setCategory(ExceptionCategory.SYSTEM_ERROR);
            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    isNull(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> escalationTeamCaptor = ArgumentCaptor.forClass(String.class);
            verify(alertPublisher).publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), escalationTeamCaptor.capture(), anyBoolean(),
                    anyString(), isNull(), anyString(), anyString());

            assertThat(escalationTeamCaptor.getValue()).isEqualTo("ENGINEERING");
        }

        @Test
        @DisplayName("Should assign CUSTOMER_SUCCESS team for customer impact alerts")
        void shouldAssignCustomerSuccessTeamForCustomerImpactAlerts() {
            // Given
            baseException.setCustomerId("CUST001");
            when(exceptionRepository.countByTimestampBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                    .thenReturn(15L);

            Page<InterfaceException> mockPage = new PageImpl<>(Collections.singletonList(baseException));
            when(exceptionRepository.findWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(mockPage);

            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    anyInt(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> escalationTeamCaptor = ArgumentCaptor.forClass(String.class);
            verify(alertPublisher).publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), escalationTeamCaptor.capture(), anyBoolean(),
                    anyString(), anyInt(), anyString(), anyString());

            assertThat(escalationTeamCaptor.getValue()).isEqualTo("CUSTOMER_SUCCESS");
        }
    }

    @Nested
    @DisplayName("Impact Assessment")
    class ImpactAssessmentTests {

        @Test
        @DisplayName("Should calculate HIGH impact for ORDER interface with CRITICAL severity")
        void shouldCalculateHighImpactForOrderInterface() {
            // Given
            baseException.setSeverity(ExceptionSeverity.CRITICAL);
            baseException.setInterfaceType(InterfaceType.ORDER);
            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    isNull(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> estimatedImpactCaptor = ArgumentCaptor.forClass(String.class);
            verify(alertPublisher).publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(),
                    estimatedImpactCaptor.capture(), isNull(), anyString(), anyString());

            assertThat(estimatedImpactCaptor.getValue()).isEqualTo("HIGH");
        }

        @Test
        @DisplayName("Should calculate SEVERE impact for system errors")
        void shouldCalculateSevereImpactForSystemErrors() {
            // Given
            baseException.setCategory(ExceptionCategory.SYSTEM_ERROR);
            when(alertPublisher.publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString(),
                    isNull(), anyString(), anyString())).thenReturn(mockFuture);

            // When
            alertingService.evaluateAndAlert(baseException);

            // Then
            ArgumentCaptor<String> estimatedImpactCaptor = ArgumentCaptor.forClass(String.class);
            verify(alertPublisher).publishCriticalAlert(anyLong(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyBoolean(),
                    estimatedImpactCaptor.capture(), isNull(), anyString(), anyString());

            assertThat(estimatedImpactCaptor.getValue()).isEqualTo("SEVERE");
        }
    }
}