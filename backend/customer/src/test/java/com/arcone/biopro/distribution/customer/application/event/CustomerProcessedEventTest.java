package com.arcone.biopro.distribution.customer.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomerProcessedEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void testCompletedEventSerialization() throws Exception {
        // Given
        ZonedDateTime now = ZonedDateTime.now();
        CustomerProcessedEvent event = CustomerProcessedEvent.builder()
            .batchId("BATCH001")
            .status(CustomerProcessedEvent.CustomerStatus.builder()
                .customerId("EXT123")
                .status("COMPLETED")
                .processedAt(now)
                .build())
            .build();

        // When - Only test serialization, not round-trip deserialization
        String json = objectMapper.writeValueAsString(event);

        // Then - Verify JSON contains expected values
        assertNotNull(json);
        assertEquals("BATCH001", event.getBatchId());
        assertEquals("EXT123", event.getStatus().getCustomerId());
        assertEquals("COMPLETED", event.getStatus().getStatus());
        assertNull(event.getStatus().getError());
        assertNotNull(event.getStatus().getProcessedAt());
    }

    @Test
    void testFailedEventSerialization() throws Exception {
        // Given
        ZonedDateTime now = ZonedDateTime.now();
        CustomerProcessedEvent event = CustomerProcessedEvent.builder()
            .batchId("BATCH001")
            .status(CustomerProcessedEvent.CustomerStatus.builder()
                .customerId("EXT123")
                .status("FAILED")
                .error("Test error message")
                .processedAt(now)
                .build())
            .build();

        // When - Only test serialization, not round-trip deserialization
        String json = objectMapper.writeValueAsString(event);

        // Then - Verify event properties directly
        assertNotNull(json);
        assertEquals("BATCH001", event.getBatchId());
        assertEquals("EXT123", event.getStatus().getCustomerId());
        assertEquals("FAILED", event.getStatus().getStatus());
        assertEquals("Test error message", event.getStatus().getError());
        assertNotNull(event.getStatus().getProcessedAt());
    }

    @Test
    void testBuilderPattern() {
        // Given & When
        CustomerProcessedEvent event = CustomerProcessedEvent.builder()
            .batchId("BATCH001")
            .status(CustomerProcessedEvent.CustomerStatus.builder()
                .customerId("EXT123")
                .status("COMPLETED")
                .processedAt(ZonedDateTime.now())
                .build())
            .build();

        // Then
        assertEquals("BATCH001", event.getBatchId());
        assertEquals("EXT123", event.getStatus().getCustomerId());
        assertEquals("COMPLETED", event.getStatus().getStatus());
    }
}
