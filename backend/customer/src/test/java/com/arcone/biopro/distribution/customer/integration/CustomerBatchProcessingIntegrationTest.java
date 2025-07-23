package com.arcone.biopro.distribution.customer.integration;

import com.arcone.biopro.distribution.customer.adapter.in.messaging.CustomerDataListener;
import com.arcone.biopro.distribution.customer.application.dto.CustomerAddressDto;
import com.arcone.biopro.distribution.customer.application.dto.CustomerBatchRequestDto;
import com.arcone.biopro.distribution.customer.application.dto.CustomerDto;
import com.arcone.biopro.distribution.customer.domain.service.CustomerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerBatchProcessingIntegrationTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CustomerDataListener customerDataListener;

    @Captor
    private ArgumentCaptor<List<CustomerDto>> customersCaptor;

    private CustomerBatchRequestDto batchRequest;
    private String validMessage;

    @BeforeEach
    void setUp() throws Exception {
        // Setup test data
        CustomerDto customer1 = new CustomerDto();
        customer1.setExternalId("EXT123");
        customer1.setName("Test Customer 1");
        customer1.setCode("CUST001");
        customer1.setType(1);
        customer1.setStatus("Y");

        CustomerAddressDto address1 = new CustomerAddressDto();
        address1.setContactName("John Doe");
        address1.setAddressType("BILLING");
        address1.setAddressLine1("123 Main St");
        address1.setCity("Test City");
        address1.setState("TS");
        address1.setPostalCode("12345");
        address1.setCountry("USA");
        customer1.setCustomerAddresses(Collections.singletonList(address1));

        CustomerDto customer2 = new CustomerDto();
        customer2.setExternalId("EXT456");
        customer2.setName("Test Customer 2");
        customer2.setCode("CUST002");
        customer2.setType(2);
        customer2.setStatus("Y");
        customer2.setCustomerAddresses(Collections.emptyList());

        batchRequest = new CustomerBatchRequestDto();
        batchRequest.setBatchId("BATCH001");
        batchRequest.setCustomers(Arrays.asList(customer1, customer2));

        validMessage = "{"
            + "\"eventId\": \"123e4567-e89b-12d3-a456-426614174000\","
            + "\"occurredOn\": \"2023-12-01T10:30:00Z\","
            + "\"eventType\": \"CustomerDataReceived\","
            + "\"eventVersion\": \"1.0\","
            + "\"payload\": {\"batchId\":\"BATCH001\",\"customers\":[{\"externalId\":\"EXT123\"},{\"externalId\":\"EXT456\"}]}"
            + "}";
    }

    @Test
    void processBatch_ValidMessage_ProcessesAllCustomers() throws Exception {
        // Given
        JsonNode eventNode = org.mockito.Mockito.mock(JsonNode.class);
        JsonNode payloadNode = org.mockito.Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(validMessage)).thenReturn(eventNode);
        when(eventNode.get("payload")).thenReturn(payloadNode);
        when(objectMapper.treeToValue(payloadNode, CustomerBatchRequestDto.class)).thenReturn(batchRequest);
        when(customerService.processBatch(eq("BATCH001"), any())).thenReturn(Mono.empty());

        // When
        customerDataListener.handleCustomerData(validMessage);

        // Then
        verify(objectMapper).readTree(validMessage);
        verify(customerService).processBatch(eq("BATCH001"), customersCaptor.capture());

        List<CustomerDto> capturedCustomers = customersCaptor.getValue();
        assertEquals(2, capturedCustomers.size());
        assertEquals("EXT123", capturedCustomers.get(0).getExternalId());
        assertEquals("EXT456", capturedCustomers.get(1).getExternalId());
    }

    @Test
    void processBatch_InvalidJson_LogsError() throws Exception {
        // Given
        String invalidJson = "invalid json";
        when(objectMapper.readTree(invalidJson))
            .thenThrow(new RuntimeException("Invalid JSON"));

        // When
        customerDataListener.handleCustomerData(invalidJson);

        // Then
        verify(objectMapper).readTree(invalidJson);
        verify(customerService, times(0)).processBatch(any(), any());
    }
}
