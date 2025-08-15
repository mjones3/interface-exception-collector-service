package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.kafka.publisher.AlertPublisher;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.config.LoggingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for evaluating exceptions and generating critical alerts
 * based on defined business rules and thresholds.
 * Implements requirement US-015 for critical exception alerting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertingService {

    private final AlertPublisher alertPublisher;
    private final InterfaceExceptionRepository exceptionRepository;
    private final MetricsService metricsService;

    // Configuration constants for alerting thresholds
    private static final int MULTIPLE_RETRY_THRESHOLD = 3;
    private static final int HIGH_CUSTOMER_IMPACT_THRESHOLD = 10;
    private static final int SEVERE_CUSTOMER_IMPACT_THRESHOLD = 50;

    /**
     * Evaluates an exception and generates appropriate alerts based on defined
     * rules.
     * This is the main entry point for alert evaluation.
     *
     * @param exception the exception to evaluate for alerting
     */
    public void evaluateAndAlert(InterfaceException exception) {
        // Set logging context
        LoggingConfig.LoggingContext.setTransactionId(exception.getTransactionId());
        LoggingConfig.LoggingContext.setInterfaceType(exception.getInterfaceType().name());

        log.debug("Evaluating exception for alerting: transactionId={}, severity={}, retryCount={}",
                exception.getTransactionId(), exception.getSeverity(), exception.getRetryCount());

        try {
            // Rule 1: Critical severity exceptions
            if (shouldAlertForCriticalSeverity(exception)) {
                generateCriticalSeverityAlert(exception);
            }

            // Rule 2: Multiple retry failures
            if (shouldAlertForMultipleRetries(exception)) {
                generateMultipleRetriesAlert(exception);
            }

            // Rule 3: System error exceptions
            if (shouldAlertForSystemError(exception)) {
                generateSystemErrorAlert(exception);
            }

            // Rule 4: Customer impact assessment
            if (shouldAlertForCustomerImpact(exception)) {
                generateCustomerImpactAlert(exception);
            }
        } finally {
            LoggingConfig.LoggingContext.clearKeys("transactionId", "interfaceType");
        }
    }

    /**
     * Determines if an exception should trigger a critical severity alert.
     *
     * @param exception the exception to evaluate
     * @return true if alert should be generated
     */
    private boolean shouldAlertForCriticalSeverity(InterfaceException exception) {
        return ExceptionSeverity.CRITICAL.equals(exception.getSeverity());
    }

    /**
     * Determines if an exception should trigger a multiple retries failed alert.
     *
     * @param exception the exception to evaluate
     * @return true if alert should be generated
     */
    private boolean shouldAlertForMultipleRetries(InterfaceException exception) {
        return exception.getRetryCount() > MULTIPLE_RETRY_THRESHOLD;
    }

    /**
     * Determines if an exception should trigger a system error alert.
     *
     * @param exception the exception to evaluate
     * @return true if alert should be generated
     */
    private boolean shouldAlertForSystemError(InterfaceException exception) {
        return ExceptionCategory.SYSTEM_ERROR.equals(exception.getCategory());
    }

    /**
     * Determines if an exception should trigger a customer impact alert.
     * This evaluates the broader impact by checking related exceptions for the same
     * customer.
     *
     * @param exception the exception to evaluate
     * @return true if alert should be generated
     */
    private boolean shouldAlertForCustomerImpact(InterfaceException exception) {
        if (exception.getCustomerId() == null) {
            return false;
        }

        // Count recent exceptions for the same customer (last 24 hours)
        OffsetDateTime oneDayAgo = OffsetDateTime.now().minusDays(1);
        long customerExceptionCount = exceptionRepository.countByTimestampBetween(oneDayAgo, OffsetDateTime.now());

        return customerExceptionCount >= HIGH_CUSTOMER_IMPACT_THRESHOLD;
    }

    /**
     * Generates a critical severity alert for exceptions with CRITICAL severity.
     *
     * @param exception the exception triggering the alert
     */
    private void generateCriticalSeverityAlert(InterfaceException exception) {
        AlertLevel alertLevel = AlertLevel.CRITICAL;
        AlertReason alertReason = AlertReason.CRITICAL_SEVERITY;
        EstimatedImpact estimatedImpact = calculateImpactForCriticalSeverity(exception);
        String escalationTeam = determineEscalationTeam(alertLevel, alertReason);

        log.warn("Generating critical severity alert for exception: transactionId={}, severity={}",
                exception.getTransactionId(), exception.getSeverity());

        // Record metrics for critical alert
        metricsService.recordCriticalAlert(alertReason.name(), exception.getInterfaceType());

        publishAlert(exception, alertLevel, alertReason, estimatedImpact, escalationTeam);
    }

    /**
     * Generates a multiple retries failed alert for exceptions exceeding retry
     * threshold.
     *
     * @param exception the exception triggering the alert
     */
    private void generateMultipleRetriesAlert(InterfaceException exception) {
        AlertLevel alertLevel = determineAlertLevelForRetries(exception.getRetryCount());
        AlertReason alertReason = AlertReason.MULTIPLE_RETRIES_FAILED;
        EstimatedImpact estimatedImpact = calculateImpactForMultipleRetries(exception);
        String escalationTeam = determineEscalationTeam(alertLevel, alertReason);

        log.warn("Generating multiple retries alert for exception: transactionId={}, retryCount={}",
                exception.getTransactionId(), exception.getRetryCount());

        // Record metrics for critical alert
        metricsService.recordCriticalAlert(alertReason.name(), exception.getInterfaceType());

        publishAlert(exception, alertLevel, alertReason, estimatedImpact, escalationTeam);
    }

    /**
     * Generates a system error alert for exceptions with SYSTEM_ERROR category.
     *
     * @param exception the exception triggering the alert
     */
    private void generateSystemErrorAlert(InterfaceException exception) {
        AlertLevel alertLevel = AlertLevel.CRITICAL;
        AlertReason alertReason = AlertReason.SYSTEM_ERROR;
        EstimatedImpact estimatedImpact = calculateImpactForSystemError(exception);
        String escalationTeam = determineEscalationTeam(alertLevel, alertReason);

        log.warn("Generating system error alert for exception: transactionId={}, category={}",
                exception.getTransactionId(), exception.getCategory());

        publishAlert(exception, alertLevel, alertReason, estimatedImpact, escalationTeam);
    }

    /**
     * Generates a customer impact alert for exceptions affecting multiple
     * customers.
     *
     * @param exception the exception triggering the alert
     */
    private void generateCustomerImpactAlert(InterfaceException exception) {
        int customersAffected = calculateCustomersAffected(exception);
        AlertLevel alertLevel = determineAlertLevelForCustomerImpact(customersAffected);
        AlertReason alertReason = AlertReason.CUSTOMER_IMPACT;
        EstimatedImpact estimatedImpact = calculateImpactForCustomerImpact(customersAffected);
        String escalationTeam = determineEscalationTeam(alertLevel, alertReason);

        log.warn("Generating customer impact alert for exception: transactionId={}, customersAffected={}",
                exception.getTransactionId(), customersAffected);

        publishAlertWithCustomerCount(exception, alertLevel, alertReason, estimatedImpact, escalationTeam,
                customersAffected);
    }

    /**
     * Determines the alert level based on the number of retry attempts.
     *
     * @param retryCount the number of retry attempts
     * @return the appropriate alert level
     */
    private AlertLevel determineAlertLevelForRetries(int retryCount) {
        if (retryCount > 5) {
            return AlertLevel.EMERGENCY;
        }
        return AlertLevel.CRITICAL;
    }

    /**
     * Determines the alert level based on the number of customers affected.
     *
     * @param customersAffected the number of customers affected
     * @return the appropriate alert level
     */
    private AlertLevel determineAlertLevelForCustomerImpact(int customersAffected) {
        if (customersAffected >= SEVERE_CUSTOMER_IMPACT_THRESHOLD) {
            return AlertLevel.EMERGENCY;
        }
        return AlertLevel.CRITICAL;
    }

    /**
     * Determines the escalation team based on alert level and reason.
     *
     * @param alertLevel  the level of the alert
     * @param alertReason the reason for the alert
     * @return the appropriate escalation team
     */
    private String determineEscalationTeam(AlertLevel alertLevel, AlertReason alertReason) {
        if (AlertLevel.EMERGENCY.equals(alertLevel)) {
            return "MANAGEMENT";
        }

        switch (alertReason) {
            case SYSTEM_ERROR:
                return "ENGINEERING";
            case CUSTOMER_IMPACT:
                return "CUSTOMER_SUCCESS";
            case MULTIPLE_RETRIES_FAILED:
            case CRITICAL_SEVERITY:
            default:
                return "OPERATIONS";
        }
    }

    /**
     * Calculates the estimated impact for critical severity exceptions.
     *
     * @param exception the exception to assess
     * @return the estimated impact level
     */
    private EstimatedImpact calculateImpactForCriticalSeverity(InterfaceException exception) {
        // Critical severity exceptions are considered high impact by default
        // Additional factors can be considered based on interface type
        switch (exception.getInterfaceType()) {
            case ORDER:
                return EstimatedImpact.HIGH;
            case COLLECTION:
                return EstimatedImpact.SEVERE;
            case DISTRIBUTION:
                return EstimatedImpact.HIGH;
            default:
                return EstimatedImpact.MEDIUM;
        }
    }

    /**
     * Calculates the estimated impact for multiple retry failures.
     *
     * @param exception the exception to assess
     * @return the estimated impact level
     */
    private EstimatedImpact calculateImpactForMultipleRetries(InterfaceException exception) {
        int retryCount = exception.getRetryCount();

        if (retryCount > 5) {
            return EstimatedImpact.SEVERE;
        } else if (retryCount > MULTIPLE_RETRY_THRESHOLD) {
            return EstimatedImpact.HIGH;
        }

        return EstimatedImpact.MEDIUM;
    }

    /**
     * Calculates the estimated impact for system error exceptions.
     *
     * @param exception the exception to assess
     * @return the estimated impact level
     */
    private EstimatedImpact calculateImpactForSystemError(InterfaceException exception) {
        // System errors are considered severe as they indicate infrastructure issues
        return EstimatedImpact.SEVERE;
    }

    /**
     * Calculates the estimated impact based on customer impact.
     *
     * @param customersAffected the number of customers affected
     * @return the estimated impact level
     */
    private EstimatedImpact calculateImpactForCustomerImpact(int customersAffected) {
        if (customersAffected >= SEVERE_CUSTOMER_IMPACT_THRESHOLD) {
            return EstimatedImpact.SEVERE;
        } else if (customersAffected >= HIGH_CUSTOMER_IMPACT_THRESHOLD) {
            return EstimatedImpact.HIGH;
        }

        return EstimatedImpact.MEDIUM;
    }

    /**
     * Calculates the number of customers affected by similar exceptions.
     * This is a simplified implementation that could be enhanced with more
     * sophisticated analysis.
     *
     * @param exception the exception to analyze
     * @return the estimated number of customers affected
     */
    private int calculateCustomersAffected(InterfaceException exception) {
        // For now, return a simplified calculation
        // In a real implementation, this would analyze patterns across multiple
        // exceptions
        OffsetDateTime oneHourAgo = OffsetDateTime.now().minusHours(1);

        // Count exceptions of the same type and category in the last hour
        List<InterfaceException> recentSimilarExceptions = exceptionRepository.findWithFiltersTypeSafe(
                exception.getInterfaceType(),
                null, // any status
                exception.getSeverity(),
                null, // any customer
                oneHourAgo,
                OffsetDateTime.now(),
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        // Estimate customers affected based on similar exceptions
        return Math.min(recentSimilarExceptions.size(), 100); // Cap at 100 for safety
    }

    /**
     * Publishes an alert using the AlertPublisher.
     *
     * @param exception       the exception triggering the alert
     * @param alertLevel      the level of the alert
     * @param alertReason     the reason for the alert
     * @param estimatedImpact the estimated impact
     * @param escalationTeam  the team to escalate to
     */
    private void publishAlert(InterfaceException exception, AlertLevel alertLevel, AlertReason alertReason,
            EstimatedImpact estimatedImpact, String escalationTeam) {

        String correlationId = generateCorrelationId();

        alertPublisher.publishCriticalAlert(
                exception.getId(),
                exception.getTransactionId(),
                alertLevel.name(),
                alertReason.name(),
                exception.getInterfaceType().name(),
                exception.getExceptionReason(),
                exception.getCustomerId(),
                escalationTeam,
                AlertLevel.CRITICAL.equals(alertLevel) || AlertLevel.EMERGENCY.equals(alertLevel),
                estimatedImpact.name(),
                null, // customersAffected - not applicable for basic alerts
                correlationId,
                generateCausationId());
    }

    /**
     * Publishes an alert with customer count information.
     *
     * @param exception         the exception triggering the alert
     * @param alertLevel        the level of the alert
     * @param alertReason       the reason for the alert
     * @param estimatedImpact   the estimated impact
     * @param escalationTeam    the team to escalate to
     * @param customersAffected the number of customers affected
     */
    private void publishAlertWithCustomerCount(InterfaceException exception, AlertLevel alertLevel,
            AlertReason alertReason, EstimatedImpact estimatedImpact,
            String escalationTeam, int customersAffected) {

        String correlationId = generateCorrelationId();

        alertPublisher.publishCriticalAlert(
                exception.getId(),
                exception.getTransactionId(),
                alertLevel.name(),
                alertReason.name(),
                exception.getInterfaceType().name(),
                exception.getExceptionReason(),
                exception.getCustomerId(),
                escalationTeam,
                AlertLevel.CRITICAL.equals(alertLevel) || AlertLevel.EMERGENCY.equals(alertLevel),
                estimatedImpact.name(),
                customersAffected,
                correlationId,
                generateCausationId());
    }

    /**
     * Generates a correlation ID for event tracking.
     *
     * @return a unique correlation ID
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a causation ID for event chain tracking.
     *
     * @return a unique causation ID
     */
    private String generateCausationId() {
        return UUID.randomUUID().toString();
    }
}