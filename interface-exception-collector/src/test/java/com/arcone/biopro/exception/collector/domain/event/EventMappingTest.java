package com.arcone.biopro.exception.collector.domain.event;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.outbound.ExceptionCapturedEvent;
import com.arcone.biopro.exception.collector.domain.mapper.EventMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for event mapping functionality.
 * Verifies that events can be properly mapped to entities and vice versa.
 */
@SpringBootTest
class EventMappingTest {

    @Autowired
    private EventMapper eventMapper;

    @Test
    void shouldMapOrderRejectedEventToInterfaceException() {
        // Given
        OrderRejectedEvent.OrderRejectedPayload payload = OrderRejectedEvent.OrderRejectedPayload.builder()
                .transactionId("test-transaction-123")
                .externalId("ORDER-ABC123")
                .operation("CREATE_ORDER")
                .rejectedReason("Order already exists")
                .customerId("CUST001")
                .locationCode("LOC001")
                .orderItems(List.of())
                .build();

        OrderRejectedEvent event = OrderRejectedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OrderRejected")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId(UUID.randomUUID().toString())
                .payload(payload)
                .build();

        // When
        InterfaceException exception = eventMapper.toInterfaceException(event);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getTransactionId()).isEqualTo("test-transaction-123");
        assertThat(exception.getInterfaceType()).isEqualTo(InterfaceType.ORDER);
        assertThat(exception.getExceptionReason()).isEqualTo("Order already exists");
        assertThat(exception.getOperation()).isEqualTo("CREATE_ORDER");
        assertThat(exception.getExternalId()).isEqualTo("ORDER-ABC123");
        assertThat(exception.getCustomerId()).isEqualTo("CUST001");
        assertThat(exception.getLocationCode()).isEqualTo("LOC001");
        assertThat(exception.getCategory()).isEqualTo(ExceptionCategory.BUSINESS_RULE);
        assertThat(exception.getSeverity()).isEqualTo(ExceptionSeverity.MEDIUM);
    }

    @Test
    void shouldMapInterfaceExceptionToExceptionCapturedEvent() {
        // Given
        InterfaceException exception = InterfaceException.builder()
                .id(123L)
                .transactionId("test-transaction-456")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Test exception reason")
                .operation("CREATE_ORDER")
                .externalId("ORDER-DEF456")
                .customerId("CUST002")
                .category(ExceptionCategory.VALIDATION)
                .severity(ExceptionSeverity.HIGH)
                .retryable(true)
                .timestamp(OffsetDateTime.now())
                .build();

        String correlationId = UUID.randomUUID().toString();
        String causationId = UUID.randomUUID().toString();

        // When
        ExceptionCapturedEvent capturedEvent = eventMapper.toExceptionCapturedEvent(exception, correlationId,
                causationId);

        // Then
        assertThat(capturedEvent).isNotNull();
        assertThat(capturedEvent.getEventType()).isEqualTo("ExceptionCaptured");
        assertThat(capturedEvent.getEventVersion()).isEqualTo("1.0");
        assertThat(capturedEvent.getSource()).isEqualTo("exception-collector-service");
        assertThat(capturedEvent.getCorrelationId()).isEqualTo(correlationId);
        assertThat(capturedEvent.getCausationId()).isEqualTo(causationId);

        ExceptionCapturedEvent.ExceptionCapturedPayload payload = capturedEvent.getPayload();
        assertThat(payload).isNotNull();
        assertThat(payload.getExceptionId()).isEqualTo(123L);
        assertThat(payload.getTransactionId()).isEqualTo("test-transaction-456");
        assertThat(payload.getInterfaceType()).isEqualTo("ORDER");
        assertThat(payload.getSeverity()).isEqualTo("HIGH");
        assertThat(payload.getCategory()).isEqualTo("VALIDATION");
        assertThat(payload.getExceptionReason()).isEqualTo("Test exception reason");
        assertThat(payload.getCustomerId()).isEqualTo("CUST002");
        assertThat(payload.getRetryable()).isTrue();
    }

    @Test
    void shouldMapRejectionReasonToCorrectCategory() {
        // Test business rule mapping
        ExceptionCategory businessRule = eventMapper.mapRejectionReasonToCategory("Order already exists");
        assertThat(businessRule).isEqualTo(ExceptionCategory.BUSINESS_RULE);

        // Test validation mapping
        ExceptionCategory validation = eventMapper.mapRejectionReasonToCategory("Invalid format");
        assertThat(validation).isEqualTo(ExceptionCategory.VALIDATION);

        // Test timeout mapping
        ExceptionCategory timeout = eventMapper.mapRejectionReasonToCategory("Request timed out");
        assertThat(timeout).isEqualTo(ExceptionCategory.TIMEOUT);

        // Test default mapping
        ExceptionCategory defaultCategory = eventMapper.mapRejectionReasonToCategory("Unknown error");
        assertThat(defaultCategory).isEqualTo(ExceptionCategory.SYSTEM_ERROR);
    }

    @Test
    void shouldMapRejectionReasonToCorrectSeverity() {
        // Test critical severity
        ExceptionSeverity critical = eventMapper.mapRejectionReasonToSeverity("Critical system error");
        assertThat(critical).isEqualTo(ExceptionSeverity.CRITICAL);

        // Test high severity
        ExceptionSeverity high = eventMapper.mapRejectionReasonToSeverity("Network timeout");
        assertThat(high).isEqualTo(ExceptionSeverity.HIGH);

        // Test medium severity
        ExceptionSeverity medium = eventMapper.mapRejectionReasonToSeverity("Validation failed");
        assertThat(medium).isEqualTo(ExceptionSeverity.MEDIUM);

        // Test default severity
        ExceptionSeverity defaultSeverity = eventMapper.mapRejectionReasonToSeverity("Some other error");
        assertThat(defaultSeverity).isEqualTo(ExceptionSeverity.LOW);
    }
}
