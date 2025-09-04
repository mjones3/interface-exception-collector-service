package com.arcone.biopro.exception.collector.api;

import com.arcone.biopro.exception.collector.api.dto.ExceptionDetailResponse;
import com.arcone.biopro.exception.collector.api.dto.ExceptionListResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class to verify the order data API functionality.
 * This test focuses on the DTO structure and mapping without requiring
 * the full application context.
 */
class OrderDataApiTest {

    @Test
    void exceptionDetailResponse_ShouldIncludeOrderDataFields() {
        // Given
        Map<String, Object> orderData = Map.of(
                "externalId", "ORDER-123",
                "customerId", "CUST001",
                "orderItems", java.util.List.of(
                        Map.of(
                                "productCode", "PROD-ABC123",
                                "bloodType", "O_POS",
                                "quantity", 2
                        )
                )
        );

        OffsetDateTime now = OffsetDateTime.now();

        // When
        ExceptionDetailResponse response = ExceptionDetailResponse.builder()
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
                .timestamp(now)
                .orderReceived(orderData)
                .orderRetrievalAttempted(true)
                .orderRetrievedAt(now)
                .build();

        // Then
        assertThat(response.getOrderReceived()).isNotNull();
        assertThat(response.getOrderReceived()).isEqualTo(orderData);
        assertThat(response.getOrderRetrievalAttempted()).isTrue();
        assertThat(response.getOrderRetrievedAt()).isEqualTo(now);
        assertThat(response.getOrderRetrievalError()).isNull();
    }

    @Test
    void exceptionDetailResponse_ShouldHandleOrderRetrievalError() {
        // Given
        String errorMessage = "Connection timeout to order service";

        // When
        ExceptionDetailResponse response = ExceptionDetailResponse.builder()
                .id(1L)
                .transactionId("test-transaction-456")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception reason")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.BUSINESS_RULE)
                .retryable(true)
                .customerId("CUST001")
                .timestamp(OffsetDateTime.now())
                .orderReceived(null)
                .orderRetrievalAttempted(true)
                .orderRetrievalError(errorMessage)
                .orderRetrievedAt(null)
                .build();

        // Then
        assertThat(response.getOrderReceived()).isNull();
        assertThat(response.getOrderRetrievalAttempted()).isTrue();
        assertThat(response.getOrderRetrievalError()).isEqualTo(errorMessage);
        assertThat(response.getOrderRetrievedAt()).isNull();
    }

    @Test
    void exceptionListResponse_ShouldIncludeHasOrderDataField() {
        // When
        ExceptionListResponse response = ExceptionListResponse.builder()
                .id(1L)
                .transactionId("test-transaction-789")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception reason")
                .operation("CREATE_ORDER")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.BUSINESS_RULE)
                .retryable(true)
                .customerId("CUST001")
                .timestamp(OffsetDateTime.now())
                .hasOrderData(true)
                .build();

        // Then
        assertThat(response.getHasOrderData()).isTrue();
    }

    @Test
    void interfaceException_ShouldSupportOrderDataFields() {
        // Given
        Map<String, Object> orderData = Map.of(
                "externalId", "ORDER-456",
                "customerId", "CUST002"
        );

        OffsetDateTime now = OffsetDateTime.now();

        // When
        InterfaceException exception = InterfaceException.builder()
                .id(1L)
                .transactionId("test-transaction-999")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception reason")
                .operation("CREATE_ORDER")
                .externalId("ORDER-456")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.BUSINESS_RULE)
                .retryable(true)
                .customerId("CUST002")
                .locationCode("LOC001")
                .timestamp(now)
                .processedAt(now)
                .orderReceived(orderData)
                .orderRetrievalAttempted(true)
                .orderRetrievedAt(now)
                .build();

        // Then
        assertThat(exception.getOrderReceived()).isNotNull();
        assertThat(exception.getOrderReceived()).isEqualTo(orderData);
        assertThat(exception.getOrderRetrievalAttempted()).isTrue();
        assertThat(exception.getOrderRetrievedAt()).isEqualTo(now);
        assertThat(exception.getOrderRetrievalError()).isNull();
    }
}