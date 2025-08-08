package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.api.dto.AcknowledgeRequest;
import com.arcone.biopro.exception.collector.api.dto.AcknowledgeResponse;
import com.arcone.biopro.exception.collector.api.dto.ResolveRequest;
import com.arcone.biopro.exception.collector.api.dto.ResolveResponse;
import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for ManagementController.
 * Tests acknowledgment and resolution endpoints as per requirements US-013 and
 * US-014.
 */
@WebMvcTest(ManagementController.class)
@DisplayName("ManagementController Tests")
class ManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExceptionManagementService managementService;

    private final String transactionId = "test-transaction-123";
    private final String acknowledgedBy = "john.doe@company.com";
    private final String resolvedBy = "jane.smith@company.com";

    @BeforeEach
    void setUp() {
        // Setup common test data if needed
    }

    @Test
    @DisplayName("Should acknowledge exception successfully")
    void shouldAcknowledgeExceptionSuccessfully() throws Exception {
        // Given
        AcknowledgeRequest request = AcknowledgeRequest.builder()
                .acknowledgedBy(acknowledgedBy)
                .notes("Reviewed and assigned to development team")
                .build();

        AcknowledgeResponse response = AcknowledgeResponse.builder()
                .status("ACKNOWLEDGED")
                .acknowledgedAt(OffsetDateTime.now())
                .acknowledgedBy(acknowledgedBy)
                .notes("Reviewed and assigned to development team")
                .transactionId(transactionId)
                .build();

        when(managementService.canAcknowledge(transactionId)).thenReturn(true);
        when(managementService.acknowledgeException(eq(transactionId), any(AcknowledgeRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/exceptions/{transactionId}/acknowledge", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.acknowledgedBy").value(acknowledgedBy))
                .andExpect(jsonPath("$.notes").value("Reviewed and assigned to development team"))
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.acknowledgedAt").exists());
    }

    @Test
    @DisplayName("Should return 409 when exception cannot be acknowledged")
    void shouldReturn409WhenExceptionCannotBeAcknowledged() throws Exception {
        // Given
        AcknowledgeRequest request = AcknowledgeRequest.builder()
                .acknowledgedBy(acknowledgedBy)
                .notes("Test notes")
                .build();

        when(managementService.canAcknowledge(transactionId)).thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/v1/exceptions/{transactionId}/acknowledge", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("ACKNOWLEDGMENT_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message")
                        .value("Exception cannot be acknowledged (not found, already resolved, or closed)"))
                .andExpect(jsonPath("$.path").value("/api/v1/exceptions/" + transactionId + "/acknowledge"));
    }

    @Test
    @DisplayName("Should return 404 when exception not found for acknowledgment")
    void shouldReturn404WhenExceptionNotFoundForAcknowledgment() throws Exception {
        // Given
        AcknowledgeRequest request = AcknowledgeRequest.builder()
                .acknowledgedBy(acknowledgedBy)
                .notes("Test notes")
                .build();

        when(managementService.canAcknowledge(transactionId)).thenReturn(true);
        when(managementService.acknowledgeException(eq(transactionId), any(AcknowledgeRequest.class)))
                .thenThrow(new IllegalArgumentException("Exception not found with transaction ID: " + transactionId));

        // When & Then
        mockMvc.perform(put("/api/v1/exceptions/{transactionId}/acknowledge", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("EXCEPTION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Exception not found with transaction ID: " + transactionId))
                .andExpect(jsonPath("$.path").value("/api/v1/exceptions/" + transactionId + "/acknowledge"));
    }

    @Test
    @DisplayName("Should return 400 when acknowledgment request is invalid")
    void shouldReturn400WhenAcknowledgmentRequestIsInvalid() throws Exception {
        // Given - request with missing required field
        AcknowledgeRequest request = AcknowledgeRequest.builder()
                .notes("Test notes")
                // Missing acknowledgedBy
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/exceptions/{transactionId}/acknowledge", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should resolve exception successfully")
    void shouldResolveExceptionSuccessfully() throws Exception {
        // Given
        ResolveRequest request = ResolveRequest.builder()
                .resolvedBy(resolvedBy)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Fixed data validation issue in source system")
                .build();

        ResolveResponse response = ResolveResponse.builder()
                .status("RESOLVED")
                .resolvedAt(OffsetDateTime.now())
                .resolvedBy(resolvedBy)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Fixed data validation issue in source system")
                .transactionId(transactionId)
                .totalRetryAttempts(2)
                .build();

        when(managementService.canResolve(transactionId)).thenReturn(true);
        when(managementService.resolveException(eq(transactionId), any(ResolveRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/exceptions/{transactionId}/resolve", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedBy").value(resolvedBy))
                .andExpect(jsonPath("$.resolutionMethod").value("MANUAL_RESOLUTION"))
                .andExpect(jsonPath("$.resolutionNotes").value("Fixed data validation issue in source system"))
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.totalRetryAttempts").value(2))
                .andExpect(jsonPath("$.resolvedAt").exists());
    }

    @Test
    @DisplayName("Should return 409 when exception cannot be resolved")
    void shouldReturn409WhenExceptionCannotBeResolved() throws Exception {
        // Given
        ResolveRequest request = ResolveRequest.builder()
                .resolvedBy(resolvedBy)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test notes")
                .build();

        when(managementService.canResolve(transactionId)).thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/v1/exceptions/{transactionId}/resolve", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("RESOLUTION_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message")
                        .value("Exception cannot be resolved (not found, already resolved, or closed)"))
                .andExpect(jsonPath("$.path").value("/api/v1/exceptions/" + transactionId + "/resolve"));
    }

    @Test
    @DisplayName("Should return 404 when exception not found for resolution")
    void shouldReturn404WhenExceptionNotFoundForResolution() throws Exception {
        // Given
        ResolveRequest request = ResolveRequest.builder()
                .resolvedBy(resolvedBy)
                .resolutionMethod(ResolutionMethod.MANUAL_RESOLUTION)
                .resolutionNotes("Test notes")
                .build();

        when(managementService.canResolve(transactionId)).thenReturn(true);
        when(managementService.resolveException(eq(transactionId), any(ResolveRequest.class)))
                .thenThrow(new IllegalArgumentException("Exception not found with transaction ID: " + transactionId));

        // When & Then
        mockMvc.perform(put("/api/v1/exceptions/{transactionId}/resolve", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("EXCEPTION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Exception not found with transaction ID: " + transactionId))
                .andExpect(jsonPath("$.path").value("/api/v1/exceptions/" + transactionId + "/resolve"));
    }

    @Test
    @DisplayName("Should return 400 when resolution request is invalid")
    void shouldReturn400WhenResolutionRequestIsInvalid() throws Exception {
        // Given - request with missing required fields
        ResolveRequest request = ResolveRequest.builder()
                .resolutionNotes("Test notes")
                // Missing resolvedBy and resolutionMethod
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/exceptions/{transactionId}/resolve", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle internal server error gracefully")
    void shouldHandleInternalServerErrorGracefully() throws Exception {
        // Given
        AcknowledgeRequest request = AcknowledgeRequest.builder()
                .acknowledgedBy(acknowledgedBy)
                .notes("Test notes")
                .build();

        when(managementService.canAcknowledge(transactionId)).thenReturn(true);
        when(managementService.acknowledgeException(eq(transactionId), any(AcknowledgeRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(put("/api/v1/exceptions/{transactionId}/acknowledge", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("ACKNOWLEDGMENT_FAILED"))
                .andExpect(jsonPath("$.message").value("Failed to acknowledge exception"))
                .andExpect(jsonPath("$.path").value("/api/v1/exceptions/" + transactionId + "/acknowledge"));
    }
}