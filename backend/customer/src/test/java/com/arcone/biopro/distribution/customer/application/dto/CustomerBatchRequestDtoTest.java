package com.arcone.biopro.distribution.customer.application.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerBatchRequestDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerialization() throws Exception {
        // Given
        CustomerDto customer1 = new CustomerDto();
        customer1.setExternalId("EXT123");
        customer1.setName("Test Customer 1");

        CustomerDto customer2 = new CustomerDto();
        customer2.setExternalId("EXT456");
        customer2.setName("Test Customer 2");

        List<CustomerDto> customers = Arrays.asList(customer1, customer2);

        CustomerBatchRequestDto batchRequest = new CustomerBatchRequestDto();
        batchRequest.setBatchId("BATCH001");
        batchRequest.setCustomers(customers);

        // When
        String json = objectMapper.writeValueAsString(batchRequest);
        CustomerBatchRequestDto deserialized = objectMapper.readValue(json, CustomerBatchRequestDto.class);

        // Then
        assertNotNull(deserialized);
        assertEquals("BATCH001", deserialized.getBatchId());
        assertEquals(2, deserialized.getCustomers().size());
        assertEquals("EXT123", deserialized.getCustomers().get(0).getExternalId());
        assertEquals("Test Customer 1", deserialized.getCustomers().get(0).getName());
        assertEquals("EXT456", deserialized.getCustomers().get(1).getExternalId());
        assertEquals("Test Customer 2", deserialized.getCustomers().get(1).getName());
    }

    @Test
    void testDeserialization() throws Exception {
        // Given
        String json = "{\"batchId\":\"BATCH001\",\"customers\":[{\"externalId\":\"EXT123\",\"name\":\"Test Customer 1\"},{\"externalId\":\"EXT456\",\"name\":\"Test Customer 2\"}]}";

        // When
        CustomerBatchRequestDto deserialized = objectMapper.readValue(json, CustomerBatchRequestDto.class);

        // Then
        assertNotNull(deserialized);
        assertEquals("BATCH001", deserialized.getBatchId());
        assertEquals(2, deserialized.getCustomers().size());
        assertEquals("EXT123", deserialized.getCustomers().get(0).getExternalId());
        assertEquals("Test Customer 1", deserialized.getCustomers().get(0).getName());
        assertEquals("EXT456", deserialized.getCustomers().get(1).getExternalId());
        assertEquals("Test Customer 2", deserialized.getCustomers().get(1).getName());
    }
}
