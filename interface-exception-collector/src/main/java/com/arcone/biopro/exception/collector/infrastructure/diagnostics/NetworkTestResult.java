package com.arcone.biopro.exception.collector.infrastructure.diagnostics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the result of a network connectivity test.
 * Contains test outcome, timing information, and detailed diagnostic data.
 */
@Data
@Builder
public class NetworkTestResult {

    private final String testName;
    private final boolean success;
    private final String summary;
    private final LocalDateTime timestamp;
    private final long durationMs;
    private final Exception exception;
    private final Map<String, Object> details;

    /**
     * Creates a successful test result.
     * 
     * @param testName name of the test
     * @param summary summary of the test result
     * @param durationMs test duration in milliseconds
     * @return successful NetworkTestResult
     */
    public static NetworkTestResult success(String testName, String summary, long durationMs) {
        return NetworkTestResult.builder()
                .testName(testName)
                .success(true)
                .summary(summary)
                .timestamp(LocalDateTime.now())
                .durationMs(durationMs)
                .details(new HashMap<>())
                .build();
    }

    /**
     * Creates a successful test result with additional details.
     * 
     * @param testName name of the test
     * @param summary summary of the test result
     * @param durationMs test duration in milliseconds
     * @param details additional test details
     * @return successful NetworkTestResult with details
     */
    public static NetworkTestResult success(String testName, String summary, long durationMs, Map<String, Object> details) {
        return NetworkTestResult.builder()
                .testName(testName)
                .success(true)
                .summary(summary)
                .timestamp(LocalDateTime.now())
                .durationMs(durationMs)
                .details(details != null ? details : new HashMap<>())
                .build();
    }

    /**
     * Creates a failed test result.
     * 
     * @param testName name of the test
     * @param summary summary of the test result
     * @param durationMs test duration in milliseconds
     * @param exception the exception that caused the failure
     * @return failed NetworkTestResult
     */
    public static NetworkTestResult failure(String testName, String summary, long durationMs, Exception exception) {
        return NetworkTestResult.builder()
                .testName(testName)
                .success(false)
                .summary(summary)
                .timestamp(LocalDateTime.now())
                .durationMs(durationMs)
                .exception(exception)
                .details(new HashMap<>())
                .build();
    }

    /**
     * Creates a failed test result with additional details.
     * 
     * @param testName name of the test
     * @param summary summary of the test result
     * @param durationMs test duration in milliseconds
     * @param exception the exception that caused the failure
     * @param details additional test details
     * @return failed NetworkTestResult with details
     */
    public static NetworkTestResult failure(String testName, String summary, long durationMs, Exception exception, Map<String, Object> details) {
        return NetworkTestResult.builder()
                .testName(testName)
                .success(false)
                .summary(summary)
                .timestamp(LocalDateTime.now())
                .durationMs(durationMs)
                .exception(exception)
                .details(details != null ? details : new HashMap<>())
                .build();
    }

    /**
     * Gets a detail value by key.
     * 
     * @param key the detail key
     * @return Optional containing the detail value if present
     */
    public Optional<Object> getDetail(String key) {
        return Optional.ofNullable(details != null ? details.get(key) : null);
    }

    /**
     * Gets the exception if the test failed.
     * 
     * @return Optional containing the exception if present
     */
    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    /**
     * Adds a detail to the test result.
     * 
     * @param key the detail key
     * @param value the detail value
     */
    public void addDetail(String key, Object value) {
        if (details != null) {
            details.put(key, value);
        }
    }

    /**
     * Gets a formatted string representation of the test result.
     * 
     * @return formatted test result string
     */
    public String getFormattedResult() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] %s: %s", 
                 success ? "PASS" : "FAIL", 
                 testName, 
                 summary));
        
        if (durationMs >= 0) {
            sb.append(String.format(" (%dms)", durationMs));
        }
        
        if (!success && exception != null) {
            sb.append(String.format(" - %s", exception.getMessage()));
        }
        
        return sb.toString();
    }

    /**
     * Checks if the test completed within the specified duration.
     * 
     * @param maxDurationMs maximum allowed duration in milliseconds
     * @return true if test completed within the time limit
     */
    public boolean isWithinTimeLimit(long maxDurationMs) {
        return durationMs <= maxDurationMs;
    }

    /**
     * Gets the test result status as a string.
     * 
     * @return "SUCCESS" or "FAILURE"
     */
    public String getStatus() {
        return success ? "SUCCESS" : "FAILURE";
    }
}