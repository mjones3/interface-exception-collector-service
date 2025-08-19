package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionFilters;
import com.arcone.biopro.exception.collector.api.graphql.dto.ExceptionSummary;
import com.arcone.biopro.exception.collector.api.graphql.dto.TimeRange;
import com.arcone.biopro.exception.collector.api.graphql.service.SummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SummaryQueryResolver.
 * Tests the GraphQL query resolver for exception summary statistics.
 */
@ExtendWith(MockitoExtension.class)
class SummaryQueryResolverTest {

    @Mock
    private SummaryService summaryService;

    @InjectMocks
    private SummaryQueryResolver summaryQueryResolver;

    private TimeRange validTimeRange;
    private ExceptionFilters filters;
    private ExceptionSummary mockSummary;

    @BeforeEach
    void setUp() {
        validTimeRange = TimeRange.builder()
                .period(TimeRange.TimePeriod.LAST_24_HOURS)
                .build();

        filters = ExceptionFilters.builder()
                .excludeResolved(false)
                .build();

        mockSummary = ExceptionSummary.builder()
                .totalExceptions(100)
                .build();
    }

    @Test
    void exceptionSummary_WithValidInput_ShouldReturnSummary() throws Exception {
        // Given
        when(summaryService.generateSummary(any(TimeRange.class), any(ExceptionFilters.class)))
                .thenReturn(CompletableFuture.completedFuture(mockSummary));

        // When
        CompletableFuture<ExceptionSummary> result = summaryQueryResolver.exceptionSummary(validTimeRange, filters);
        ExceptionSummary summary = result.get();

        // Then
        assertNotNull(summary);
        assertEquals(100, summary.getTotalExceptions());
        verify(summaryService).generateSummary(validTimeRange, filters);
    }

    @Test
    void exceptionSummary_WithCustomTimeRange_ShouldValidateAndProcess() throws Exception {
        // Given
        TimeRange customTimeRange = TimeRange.builder()
                .period(TimeRange.TimePeriod.CUSTOM)
                .customRange(ExceptionFilters.DateRangeInput.builder()
                        .from(OffsetDateTime.now().minusDays(7))
                        .to(OffsetDateTime.now())
                        .build())
                .build();

        when(summaryService.generateSummary(any(TimeRange.class), any(ExceptionFilters.class)))
                .thenReturn(CompletableFuture.completedFuture(mockSummary));

        // When
        CompletableFuture<ExceptionSummary> result = summaryQueryResolver.exceptionSummary(customTimeRange, filters);
        ExceptionSummary summary = result.get();

        // Then
        assertNotNull(summary);
        verify(summaryService).generateSummary(customTimeRange, filters);
    }

    @Test
    void exceptionSummary_WithCustomTimeRangeButNoDateRange_ShouldThrowException() {
        // Given
        TimeRange invalidTimeRange = TimeRange.builder()
                .period(TimeRange.TimePeriod.CUSTOM)
                .customRange(null)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            summaryQueryResolver.exceptionSummary(invalidTimeRange, filters);
        });

        assertEquals("Custom date range is required when period is set to CUSTOM", exception.getMessage());
    }

    @Test
    void exceptionSummary_WithInvalidDateRange_ShouldThrowException() {
        // Given
        TimeRange invalidTimeRange = TimeRange.builder()
                .period(TimeRange.TimePeriod.CUSTOM)
                .customRange(ExceptionFilters.DateRangeInput.builder()
                        .from(OffsetDateTime.now())
                        .to(OffsetDateTime.now().minusDays(1)) // 'to' is before 'from'
                        .build())
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            summaryQueryResolver.exceptionSummary(invalidTimeRange, filters);
        });

        assertEquals("'from' date must be before or equal to 'to' date", exception.getMessage());
    }

    @Test
    void exceptionSummary_WithDateRangeExceedingLimit_ShouldThrowException() {
        // Given
        TimeRange invalidTimeRange = TimeRange.builder()
                .period(TimeRange.TimePeriod.CUSTOM)
                .customRange(ExceptionFilters.DateRangeInput.builder()
                        .from(OffsetDateTime.now().minusDays(400)) // More than 365 days
                        .to(OffsetDateTime.now())
                        .build())
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            summaryQueryResolver.exceptionSummary(invalidTimeRange, filters);
        });

        assertEquals("Date range cannot exceed 365 days", exception.getMessage());
    }

    @Test
    void exceptionSummary_WithNullTimeRange_ShouldThrowException() {
        // When & Then
        assertThrows(Exception.class, () -> {
            summaryQueryResolver.exceptionSummary(null, filters);
        });
    }
}