package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.api.mapper.ExceptionMapper;
import com.arcone.biopro.exception.collector.application.service.ExceptionQueryService;
import com.arcone.biopro.exception.collector.application.service.PayloadRetrievalService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for ExceptionController.
 * Tests the REST API endpoints for exception management.
 */
@WebMvcTest(ExceptionController.class)
class ExceptionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ExceptionQueryService exceptionQueryService;

        @MockBean
        private PayloadRetrievalService payloadRetrievalService;

        @MockBean
        private ExceptionMapper exceptionMapper;

        @Test
        void listExceptions_ShouldReturnListResponse() throws Exception {
                // Given
                InterfaceException exception = createTestException();
                List<InterfaceException> exceptions = List.of(exception);

                when(exceptionQueryService.findExceptionsWithFilters(
                                any(), any(), any(), any(), any(), any(), any(Sort.class)))
                                .thenReturn(exceptions);

                when(exceptionMapper.toListResponse(any(List.class)))
                                .thenReturn(createMockListResponse());

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        void getExceptionDetails_WhenExceptionExists_ShouldReturnDetails() throws Exception {
                // Given
                String transactionId = "test-transaction-123";
                InterfaceException exception = createTestException();

                when(exceptionQueryService.findExceptionByTransactionId(transactionId))
                                .thenReturn(Optional.of(exception));

                when(exceptionMapper.toDetailResponse(any(InterfaceException.class)))
                                .thenReturn(createMockDetailResponse());

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/{transactionId}", transactionId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.transactionId").value(transactionId));
        }

        @Test
        void getExceptionDetails_WhenExceptionNotFound_ShouldReturn404() throws Exception {
                // Given
                String transactionId = "non-existent-transaction";

                when(exceptionQueryService.findExceptionByTransactionId(transactionId))
                                .thenReturn(Optional.empty());

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/{transactionId}", transactionId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        void getExceptionDetails_WithIncludeOrderDataTrue_ShouldReturnOrderData() throws Exception {
                // Given
                String transactionId = "test-transaction-123";
                InterfaceException exception = createTestExceptionWithOrderData();

                when(exceptionQueryService.findExceptionByTransactionId(transactionId))
                                .thenReturn(Optional.of(exception));

                when(exceptionMapper.toDetailResponseWithOrderData(any(InterfaceException.class)))
                                .thenReturn(createMockDetailResponseWithOrderData());

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/{transactionId}", transactionId)
                                .param("includeOrderData", "true")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.transactionId").value(transactionId))
                                .andExpect(jsonPath("$.orderReceived").exists())
                                .andExpect(jsonPath("$.orderRetrievalAttempted").value(true))
                                .andExpect(jsonPath("$.orderRetrievedAt").exists());
        }

        @Test
        void getExceptionDetails_WithIncludeOrderDataFalse_ShouldNotReturnOrderData() throws Exception {
                // Given
                String transactionId = "test-transaction-123";
                InterfaceException exception = createTestExceptionWithOrderData();

                when(exceptionQueryService.findExceptionByTransactionId(transactionId))
                                .thenReturn(Optional.of(exception));

                when(exceptionMapper.toDetailResponse(any(InterfaceException.class)))
                                .thenReturn(createMockDetailResponse());

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/{transactionId}", transactionId)
                                .param("includeOrderData", "false")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.transactionId").value(transactionId))
                                .andExpect(jsonPath("$.orderReceived").doesNotExist());
        }

        @Test
        void getExceptionDetails_WithBothIncludeFlags_ShouldReturnBothPayloads() throws Exception {
                // Given
                String transactionId = "test-transaction-123";
                InterfaceException exception = createTestExceptionWithOrderData();
                Object originalPayload = java.util.Map.of("originalData", "test");

                when(exceptionQueryService.findExceptionByTransactionId(transactionId))
                                .thenReturn(Optional.of(exception));

                when(exceptionMapper.toDetailResponseWithOrderData(any(InterfaceException.class)))
                                .thenReturn(createMockDetailResponseWithOrderData());

                when(payloadRetrievalService.getOriginalPayload(eq(transactionId), eq("ORDER")))
                                .thenReturn(originalPayload);

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/{transactionId}", transactionId)
                                .param("includePayload", "true")
                                .param("includeOrderData", "true")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.transactionId").value(transactionId))
                                .andExpect(jsonPath("$.originalPayload").exists())
                                .andExpect(jsonPath("$.orderReceived").exists());
        }

        @Test
        void searchExceptions_WithValidQuery_ShouldReturnResults() throws Exception {
                // Given
                InterfaceException exception = createTestException();
                List<InterfaceException> exceptions = List.of(exception);

                when(exceptionQueryService.searchExceptions(anyString(), any(List.class), any(Sort.class)))
                                .thenReturn(exceptions);

                when(exceptionMapper.toListResponse(any(List.class)))
                                .thenReturn(createMockListResponse());

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/search")
                                .param("query", "test error")
                                .param("fields", "exceptionReason")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        void searchExceptions_WithEmptyQuery_ShouldReturn400() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/search")
                                .param("query", "")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getExceptionSummary_WithValidTimeRange_ShouldReturnSummary() throws Exception {
                // Given
                when(exceptionQueryService.getExceptionSummary(anyString(), any()))
                                .thenReturn(createMockSummaryResponse());

                // When & Then
                mockMvc.perform(get("/api/v1/exceptions/summary")
                                .param("timeRange", "week")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.totalExceptions").isNumber());
        }

        private InterfaceException createTestException() {
                return InterfaceException.builder()
                                .id(1L)
                                .transactionId("test-transaction-123")
                                .interfaceType(InterfaceType.ORDER)
                                .exceptionReason("Test exception reason")
                                .operation("CREATE_ORDER")
                                .externalId("ORDER-123")
                                .status(ExceptionStatus.NEW)
                                .severity(ExceptionSeverity.MEDIUM)
                                .category(ExceptionCategory.BUSINESS_RULE)
                                .retryable(true)
                                .customerId("CUST001")
                                .locationCode("LOC001")
                                .timestamp(OffsetDateTime.now())
                                .processedAt(OffsetDateTime.now())
                                .retryCount(0)
                                .build();
        }

        private InterfaceException createTestExceptionWithOrderData() {
                java.util.Map<String, Object> orderData = java.util.Map.of(
                                "externalId", "ORDER-123",
                                "customerId", "CUST001",
                                "orderItems", List.of(
                                                java.util.Map.of(
                                                                "productCode", "PROD-ABC123",
                                                                "bloodType", "O_POS",
                                                                "quantity", 2)));

                return InterfaceException.builder()
                                .id(1L)
                                .transactionId("test-transaction-123")
                                .interfaceType(InterfaceType.ORDER)
                                .exceptionReason("Test exception reason")
                                .operation("CREATE_ORDER")
                                .externalId("ORDER-123")
                                .status(ExceptionStatus.NEW)
                                .severity(ExceptionSeverity.MEDIUM)
                                .category(ExceptionCategory.BUSINESS_RULE)
                                .retryable(true)
                                .customerId("CUST001")
                                .locationCode("LOC001")
                                .timestamp(OffsetDateTime.now())
                                .processedAt(OffsetDateTime.now())
                                .retryCount(0)
                                .orderReceived(orderData)
                                .orderRetrievalAttempted(true)
                                .orderRetrievedAt(OffsetDateTime.now())
                                .build();
        }

        private com.arcone.biopro.exception.collector.api.dto.PagedResponse<com.arcone.biopro.exception.collector.api.dto.ExceptionListResponse> createMockPagedResponse() {
                return com.arcone.biopro.exception.collector.api.dto.PagedResponse.<com.arcone.biopro.exception.collector.api.dto.ExceptionListResponse>builder()
                                .content(List.of())
                                .page(0)
                                .size(20)
                                .totalElements(1L)
                                .totalPages(1)
                                .first(true)
                                .last(true)
                                .numberOfElements(1)
                                .empty(false)
                                .build();
        }

        private List<com.arcone.biopro.exception.collector.api.dto.ExceptionListResponse> createMockListResponse() {
                return List.of();
        }

        private com.arcone.biopro.exception.collector.api.dto.ExceptionDetailResponse createMockDetailResponse() {
                return com.arcone.biopro.exception.collector.api.dto.ExceptionDetailResponse.builder()
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
                                .build();
        }

        private com.arcone.biopro.exception.collector.api.dto.ExceptionDetailResponse createMockDetailResponseWithOrderData() {
                java.util.Map<String, Object> orderData = java.util.Map.of(
                                "externalId", "ORDER-123",
                                "customerId", "CUST001",
                                "orderItems", List.of(
                                                java.util.Map.of(
                                                                "productCode", "PROD-ABC123",
                                                                "bloodType", "O_POS",
                                                                "quantity", 2)));

                return com.arcone.biopro.exception.collector.api.dto.ExceptionDetailResponse.builder()
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
                                .orderReceived(orderData)
                                .orderRetrievalAttempted(true)
                                .orderRetrievedAt(OffsetDateTime.now())
                                .build();
        }

        private com.arcone.biopro.exception.collector.api.dto.ExceptionSummaryResponse createMockSummaryResponse() {
                return com.arcone.biopro.exception.collector.api.dto.ExceptionSummaryResponse.builder()
                                .totalExceptions(100L)
                                .byInterfaceType(java.util.Map.of("ORDER", 50L, "COLLECTION", 30L, "DISTRIBUTION", 20L))
                                .bySeverity(java.util.Map.of("LOW", 20L, "MEDIUM", 60L, "HIGH", 15L, "CRITICAL", 5L))
                                .byStatus(java.util.Map.of("NEW", 40L, "ACKNOWLEDGED", 30L, "RESOLVED", 30L))
                                .trends(List.of())
                                .build();
        }
}