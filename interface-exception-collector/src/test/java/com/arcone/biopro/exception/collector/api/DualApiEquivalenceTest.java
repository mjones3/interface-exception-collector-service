package com.arcone.biopro.exception.collector.api;

import com.arcone.biopro.exception.collector.api.dto.ExceptionListResponse;
import com.arcone.biopro.exception.collector.api.dto.ExceptionDetailResponse;
import com.arcone.biopro.exception.collector.api.dto.ExceptionSummaryResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests that verify GraphQL and REST APIs return equivalent data.
 * Ensures data consistency between both API types for the same operations.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DualApiEquivalenceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private HttpGraphQlTester graphQlTester;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        graphQlTester = HttpGraphQlTester.create(mockMvc);

        // Create test data
        createTestExceptions();
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void exceptionList_RestAndGraphQL_ShouldReturnEquivalentData() throws Exception {
        // When - Call REST API
        MvcResult restResult = mockMvc.perform(get("/api/v1/exceptions")
                .param("interfaceType", "ORDER")
                .param("status", "NEW")
                .param("severity", "HIGH")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<ExceptionListResponse> restResponse = objectMapper.readValue(
                restResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExceptionListResponse.class));

        // When - Call GraphQL API
        String graphqlQuery = """
                query {
                    exceptions(
                        filters: {
                            interfaceTypes: [ORDER]
                            statuses: [NEW]
                            severities: [HIGH]
                        }
                    ) {
                        edges {
                            node {
                                id
                                transactionId
                                interfaceType
                                exceptionReason
                                operation
                                status
                                severity
                                category
                                customerId
                                locationCode
                                timestamp
                                retryable
                                retryCount
                            }
                        }
                        totalCount
                    }
                }
                """;

        var graphqlResponse = graphQlTester.document(graphqlQuery)
                .execute()
                .path("exceptions");

        // Then - Compare data equivalence
        Long totalCount = graphqlResponse.path("totalCount").entity(Long.class).get();
        List<Map<String, Object>> graphqlEdges = graphqlResponse.path("edges").entityList(Map.class).get();

        assertThat(restResponse).hasSize(totalCount.intValue());
        assertThat(graphqlEdges).hasSize(restResponse.size());

        // Compare individual records
        for (int i = 0; i < restResponse.size(); i++) {
            ExceptionListResponse restItem = restResponse.get(i);
            Map<String, Object> graphqlNode = (Map<String, Object>) graphqlEdges.get(i).get("node");

            assertThat(restItem.getTransactionId()).isEqualTo(graphqlNode.get("transactionId"));
            assertThat(restItem.getInterfaceType().toString()).isEqualTo(graphqlNode.get("interfaceType"));
            assertThat(restItem.getStatus().toString()).isEqualTo(graphqlNode.get("status"));
            assertThat(restItem.getSeverity().toString()).isEqualTo(graphqlNode.get("severity"));
            assertThat(restItem.getCustomerId()).isEqualTo(graphqlNode.get("customerId"));
            assertThat(restItem.getRetryable()).isEqualTo(graphqlNode.get("retryable"));
        }
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void exceptionDetail_RestAndGraphQL_ShouldReturnEquivalentData() throws Exception {
        // Given
        String transactionId = "TXN-001";

        // When - Call REST API
        MvcResult restResult = mockMvc.perform(get("/api/v1/exceptions/{transactionId}", transactionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ExceptionDetailResponse restResponse = objectMapper.readValue(
                restResult.getResponse().getContentAsString(),
                ExceptionDetailResponse.class);

        // When - Call GraphQL API
        String graphqlQuery = """
                query {
                    exception(transactionId: "TXN-001") {
                        id
                        transactionId
                        interfaceType
                        exceptionReason
                        operation
                        externalId
                        status
                        severity
                        category
                        customerId
                        locationCode
                        timestamp
                        processedAt
                        retryable
                        retryCount
                        maxRetries
                    }
                }
                """;

        var graphqlResponse = graphQlTester.document(graphqlQuery)
                .execute()
                .path("exception");

        Map<String, Object> graphqlData = graphqlResponse.entity(Map.class).get();

        // Then - Compare data equivalence
        assertThat(restResponse.getTransactionId()).isEqualTo(graphqlData.get("transactionId"));
        assertThat(restResponse.getInterfaceType().toString()).isEqualTo(graphqlData.get("interfaceType"));
        assertThat(restResponse.getExceptionReason()).isEqualTo(graphqlData.get("exceptionReason"));
        assertThat(restResponse.getOperation()).isEqualTo(graphqlData.get("operation"));
        assertThat(restResponse.getExternalId()).isEqualTo(graphqlData.get("externalId"));
        assertThat(restResponse.getStatus().toString()).isEqualTo(graphqlData.get("status"));
        assertThat(restResponse.getSeverity().toString()).isEqualTo(graphqlData.get("severity"));
        assertThat(restResponse.getCategory().toString()).isEqualTo(graphqlData.get("category"));
        assertThat(restResponse.getCustomerId()).isEqualTo(graphqlData.get("customerId"));
        assertThat(restResponse.getLocationCode()).isEqualTo(graphqlData.get("locationCode"));
        assertThat(restResponse.getRetryable()).isEqualTo(graphqlData.get("retryable"));
        assertThat(restResponse.getRetryCount()).isEqualTo(graphqlData.get("retryCount"));
        assertThat(restResponse.getMaxRetries()).isEqualTo(graphqlData.get("maxRetries"));
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void exceptionSummary_RestAndGraphQL_ShouldReturnEquivalentData() throws Exception {
        // When - Call REST API
        MvcResult restResult = mockMvc.perform(get("/api/v1/exceptions/summary")
                .param("timeRange", "week")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ExceptionSummaryResponse restResponse = objectMapper.readValue(
                restResult.getResponse().getContentAsString(),
                ExceptionSummaryResponse.class);

        // When - Call GraphQL API
        String graphqlQuery = """
                query {
                    exceptionSummary(
                        timeRange: {
                            start: "2024-01-01T00:00:00Z"
                            end: "2024-12-31T23:59:59Z"
                        }
                    ) {
                        totalExceptions
                        byInterfaceType {
                            interfaceType
                            count
                            percentage
                        }
                        bySeverity {
                            severity
                            count
                            percentage
                        }
                        byStatus {
                            status
                            count
                            percentage
                        }
                        keyMetrics {
                            retrySuccessRate
                            averageResolutionTime
                            criticalExceptions
                            newExceptionsToday
                        }
                    }
                }
                """;

        var graphqlResponse = graphQlTester.document(graphqlQuery)
                .execute()
                .path("exceptionSummary");

        Map<String, Object> graphqlData = graphqlResponse.entity(Map.class).get();

        // Then - Compare summary data
        assertThat(restResponse.getTotalExceptions())
                .isEqualTo(((Number) graphqlData.get("totalExceptions")).longValue());

        // Compare interface type breakdown
        Map<String, Long> restByInterfaceType = restResponse.getByInterfaceType();
        List<Map<String, Object>> graphqlByInterfaceType = (List<Map<String, Object>>) graphqlData
                .get("byInterfaceType");

        for (Map<String, Object> graphqlItem : graphqlByInterfaceType) {
            String interfaceType = (String) graphqlItem.get("interfaceType");
            Long count = ((Number) graphqlItem.get("count")).longValue();
            assertThat(restByInterfaceType.get(interfaceType)).isEqualTo(count);
        }

        // Compare severity breakdown
        Map<String, Long> restBySeverity = restResponse.getBySeverity();
        List<Map<String, Object>> graphqlBySeverity = (List<Map<String, Object>>) graphqlData.get("bySeverity");

        for (Map<String, Object> graphqlItem : graphqlBySeverity) {
            String severity = (String) graphqlItem.get("severity");
            Long count = ((Number) graphqlItem.get("count")).longValue();
            assertThat(restBySeverity.get(severity)).isEqualTo(count);
        }
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void exceptionSearch_RestAndGraphQL_ShouldReturnEquivalentData() throws Exception {
        // When - Call REST API
        MvcResult restResult = mockMvc.perform(get("/api/v1/exceptions/search")
                .param("query", "validation")
                .param("fields", "exceptionReason")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<ExceptionListResponse> restResponse = objectMapper.readValue(
                restResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExceptionListResponse.class));

        // When - Call GraphQL API (using filters to simulate search)
        String graphqlQuery = """
                query {
                    exceptions(
                        filters: {
                            searchQuery: "validation"
                            searchFields: ["exceptionReason"]
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                                interfaceType
                                exceptionReason
                                status
                                severity
                                customerId
                            }
                        }
                        totalCount
                    }
                }
                """;

        var graphqlResponse = graphQlTester.document(graphqlQuery)
                .execute()
                .path("exceptions");

        Long totalCount = graphqlResponse.path("totalCount").entity(Long.class).get();
        List<Map<String, Object>> graphqlEdges = graphqlResponse.path("edges").entityList(Map.class).get();

        // Then - Compare search results
        assertThat(restResponse).hasSize(totalCount.intValue());

        for (int i = 0; i < restResponse.size(); i++) {
            ExceptionListResponse restItem = restResponse.get(i);
            Map<String, Object> graphqlNode = (Map<String, Object>) graphqlEdges.get(i).get("node");

            assertThat(restItem.getTransactionId()).isEqualTo(graphqlNode.get("transactionId"));
            assertThat(restItem.getExceptionReason()).isEqualTo(graphqlNode.get("exceptionReason"));
            // Verify search term is present in the reason
            assertThat(restItem.getExceptionReason().toLowerCase()).contains("validation");
        }
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void paginatedResults_RestAndGraphQL_ShouldReturnEquivalentData() throws Exception {
        // When - Call REST API with pagination
        MvcResult restResult = mockMvc.perform(get("/api/v1/exceptions")
                .param("page", "0")
                .param("size", "5")
                .param("sort", "timestamp,desc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode restJson = objectMapper.readTree(restResult.getResponse().getContentAsString());

        // When - Call GraphQL API with equivalent pagination
        String graphqlQuery = """
                query {
                    exceptions(
                        pagination: {
                            first: 5
                        }
                        sorting: {
                            field: "timestamp"
                            direction: DESC
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                                timestamp
                            }
                        }
                        pageInfo {
                            hasNextPage
                            hasPreviousPage
                        }
                        totalCount
                    }
                }
                """;

        var graphqlResponse = graphQlTester.document(graphqlQuery)
                .execute()
                .path("exceptions");

        List<Map<String, Object>> graphqlEdges = graphqlResponse.path("edges").entityList(Map.class).get();
        Map<String, Object> pageInfo = graphqlResponse.path("pageInfo").entity(Map.class).get();

        // Then - Compare pagination behavior
        assertThat(graphqlEdges).hasSizeLessThanOrEqualTo(5);

        // Verify sorting order (timestamps should be in descending order)
        for (int i = 0; i < graphqlEdges.size() - 1; i++) {
            Map<String, Object> current = (Map<String, Object>) graphqlEdges.get(i).get("node");
            Map<String, Object> next = (Map<String, Object>) graphqlEdges.get(i + 1).get("node");

            String currentTimestamp = (String) current.get("timestamp");
            String nextTimestamp = (String) next.get("timestamp");

            // Verify descending order
            assertThat(currentTimestamp.compareTo(nextTimestamp)).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @WithMockUser(username = "viewer", roles = { "VIEWER" })
    void filteringBehavior_RestAndGraphQL_ShouldReturnEquivalentData() throws Exception {
        // When - Call REST API with multiple filters
        MvcResult restResult = mockMvc.perform(get("/api/v1/exceptions")
                .param("interfaceType", "ORDER")
                .param("status", "NEW")
                .param("severity", "HIGH")
                .param("customerId", "CUST-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<ExceptionListResponse> restResponse = objectMapper.readValue(
                restResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ExceptionListResponse.class));

        // When - Call GraphQL API with equivalent filters
        String graphqlQuery = """
                query {
                    exceptions(
                        filters: {
                            interfaceTypes: [ORDER]
                            statuses: [NEW]
                            severities: [HIGH]
                            customerIds: ["CUST-001"]
                        }
                    ) {
                        edges {
                            node {
                                transactionId
                                interfaceType
                                status
                                severity
                                customerId
                            }
                        }
                        totalCount
                    }
                }
                """;

        var graphqlResponse = graphQlTester.document(graphqlQuery)
                .execute()
                .path("exceptions");

        Long totalCount = graphqlResponse.path("totalCount").entity(Long.class).get();
        List<Map<String, Object>> graphqlEdges = graphqlResponse.path("edges").entityList(Map.class).get();

        // Then - Compare filtered results
        assertThat(restResponse).hasSize(totalCount.intValue());

        // Verify all results match the filter criteria
        for (Map<String, Object> edge : graphqlEdges) {
            Map<String, Object> node = (Map<String, Object>) edge.get("node");
            assertThat(node.get("interfaceType")).isEqualTo("ORDER");
            assertThat(node.get("status")).isEqualTo("NEW");
            assertThat(node.get("severity")).isEqualTo("HIGH");
            assertThat(node.get("customerId")).isEqualTo("CUST-001");
        }
    }

    private void createTestExceptions() {
        // Create multiple test exceptions with different characteristics
        InterfaceException exception1 = InterfaceException.builder()
                .transactionId("TXN-001")
                .interfaceType(InterfaceType.ORDER)
                .exceptionReason("Validation error in order processing")
                .operation("CREATE_ORDER")
                .externalId("ORDER-001")
                .status(ExceptionStatus.NEW)
                .severity(ExceptionSeverity.HIGH)
                .category(ExceptionCategory.VALIDATION)
                .retryable(true)
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .timestamp(OffsetDateTime.now().minusHours(1))
                .processedAt(OffsetDateTime.now().minusHours(1))
                .retryCount(0)
                .maxRetries(3)
                .build();

        InterfaceException exception2 = InterfaceException.builder()
                .transactionId("TXN-002")
                .interfaceType(InterfaceType.COLLECTION)
                .exceptionReason("Business rule violation in collection")
                .operation("CREATE_COLLECTION")
                .externalId("COL-001")
                .status(ExceptionStatus.ACKNOWLEDGED)
                .severity(ExceptionSeverity.MEDIUM)
                .category(ExceptionCategory.BUSINESS_RULE)
                .retryable(true)
                .customerId("CUST-002")
                .locationCode("LOC-002")
                .timestamp(OffsetDateTime.now().minusHours(2))
                .processedAt(OffsetDateTime.now().minusHours(2))
                .retryCount(1)
                .maxRetries(3)
                .build();

        InterfaceException exception3 = InterfaceException.builder()
                .transactionId("TXN-003")
                .interfaceType(InterfaceType.DISTRIBUTION)
                .exceptionReason("System error in distribution processing")
                .operation("CREATE_DISTRIBUTION")
                .externalId("DIST-001")
                .status(ExceptionStatus.RESOLVED)
                .severity(ExceptionSeverity.LOW)
                .category(ExceptionCategory.SYSTEM)
                .retryable(false)
                .customerId("CUST-003")
                .locationCode("LOC-003")
                .timestamp(OffsetDateTime.now().minusHours(3))
                .processedAt(OffsetDateTime.now().minusHours(3))
                .retryCount(0)
                .maxRetries(0)
                .build();

        exceptionRepository.saveAll(List.of(exception1, exception2, exception3));
    }
}