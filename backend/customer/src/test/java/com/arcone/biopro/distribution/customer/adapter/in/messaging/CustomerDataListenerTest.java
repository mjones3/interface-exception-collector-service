package com.arcone.biopro.distribution.customer.adapter.in.messaging;

import com.arcone.biopro.distribution.customer.application.dto.CustomerBatchRequestDto;
import com.arcone.biopro.distribution.customer.application.dto.CustomerDto;
import com.arcone.biopro.distribution.customer.domain.service.CustomerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerDataListenerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CustomerDataListener customerDataListener;

    private CustomerBatchRequestDto batchRequestDto;
    private String validMessage;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        // Setup test data
        CustomerDto customerDto = new CustomerDto();
        customerDto.setExternalId("EXT123");
        customerDto.setName("Test Customer");
        customerDto.setCode("CUST001");

        batchRequestDto = new CustomerBatchRequestDto();
        batchRequestDto.setBatchId("BATCH001");
        batchRequestDto.setCustomers(Collections.singletonList(customerDto));

        validMessage = "{"
            + "\"eventId\": \"123e4567-e89b-12d3-a456-426614174000\","
            + "\"occurredOn\": \"2023-12-01T10:30:00Z\","
            + "\"eventType\": \"CustomerDataReceived\","
            + "\"eventVersion\": \"1.0\","
            + "\"payload\": {\"batchId\":\"BATCH001\",\"customers\":[{\"externalId\":\"EXT123\"}]}"
            + "}";
    }

    @Test
    void handleCustomerData_ValidMessage_ProcessesBatch() throws JsonProcessingException {
        // Given
        JsonNode eventNode = org.mockito.Mockito.mock(JsonNode.class);
        JsonNode payloadNode = org.mockito.Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(validMessage)).thenReturn(eventNode);
        when(eventNode.get("payload")).thenReturn(payloadNode);
        when(objectMapper.treeToValue(payloadNode, CustomerBatchRequestDto.class)).thenReturn(batchRequestDto);
        when(customerService.processBatch(anyString(), anyList())).thenReturn(Mono.empty());

        // When
        customerDataListener.handleCustomerData(validMessage);

        // Then
        verify(customerService).processBatch(eq("BATCH001"), eq(batchRequestDto.getCustomers()));
    }

    @Test
    void handleCustomerData_JsonProcessingException_LogsError() throws JsonProcessingException {
        // Given
        String invalidMessage = "invalid json";
        when(objectMapper.readTree(invalidMessage))
            .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // When
        customerDataListener.handleCustomerData(invalidMessage);

        // Then
        verify(customerService, never()).processBatch(anyString(), anyList());
    }

    @Test
    void handleCustomerData_NullBatchId_LogsError() throws JsonProcessingException {
        // Given
        batchRequestDto.setBatchId(null);
        String messageWithNullBatchId = "{"
            + "\"eventId\": \"123e4567-e89b-12d3-a456-426614174000\","
            + "\"occurredOn\": \"2023-12-01T10:30:00Z\","
            + "\"eventType\": \"CustomerDataReceived\","
            + "\"eventVersion\": \"1.0\","
            + "\"payload\": {\"customers\":[{\"externalId\":\"EXT123\"}]}"
            + "}";
        JsonNode eventNode = org.mockito.Mockito.mock(JsonNode.class);
        JsonNode payloadNode = org.mockito.Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(messageWithNullBatchId)).thenReturn(eventNode);
        when(eventNode.get("payload")).thenReturn(payloadNode);
        when(objectMapper.treeToValue(payloadNode, CustomerBatchRequestDto.class)).thenReturn(batchRequestDto);
        when(customerService.processBatch(eq(null), anyList())).thenReturn(Mono.empty());

        // When
        customerDataListener.handleCustomerData(messageWithNullBatchId);

        // Then
        verify(customerService).processBatch(eq(null), eq(batchRequestDto.getCustomers()));
    }

    @Test
    void handleCustomerData_EmptyCustomersList_ProcessesBatch() throws JsonProcessingException {
        // Given
        batchRequestDto.setCustomers(Collections.emptyList());
        String messageWithEmptyCustomers = "{"
            + "\"eventId\": \"123e4567-e89b-12d3-a456-426614174000\","
            + "\"occurredOn\": \"2023-12-01T10:30:00Z\","
            + "\"eventType\": \"CustomerDataReceived\","
            + "\"eventVersion\": \"1.0\","
            + "\"payload\": {\"batchId\":\"BATCH001\",\"customers\":[]}"
            + "}";
        JsonNode eventNode = org.mockito.Mockito.mock(JsonNode.class);
        JsonNode payloadNode = org.mockito.Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(messageWithEmptyCustomers)).thenReturn(eventNode);
        when(eventNode.get("payload")).thenReturn(payloadNode);
        when(objectMapper.treeToValue(payloadNode, CustomerBatchRequestDto.class)).thenReturn(batchRequestDto);
        when(customerService.processBatch(eq("BATCH001"), anyList())).thenReturn(Mono.empty());

        // When
        customerDataListener.handleCustomerData(messageWithEmptyCustomers);

        // Then
        verify(customerService).processBatch(eq("BATCH001"), eq(Collections.emptyList()));
    }
}
