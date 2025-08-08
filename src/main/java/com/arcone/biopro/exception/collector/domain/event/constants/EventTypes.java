package com.arcone.biopro.exception.collector.domain.event.constants;

/**
 * Constants for Kafka event types used throughout the Interface Exception
 * Collector Service.
 * Provides centralized definition of event type strings to ensure consistency.
 */
public final class EventTypes {

    private EventTypes() {
        // Utility class - prevent instantiation
    }

    // ========== Inbound Event Types ==========

    /**
     * Event type for order rejection events from Order Service.
     */
    public static final String ORDER_REJECTED = "OrderRejected";

    /**
     * Event type for order cancellation events from Order Service.
     */
    public static final String ORDER_CANCELLED = "OrderCancelled";

    /**
     * Event type for collection rejection events from Collection Service.
     */
    public static final String COLLECTION_REJECTED = "CollectionRejected";

    /**
     * Event type for distribution failure events from Distribution Service.
     */
    public static final String DISTRIBUTION_FAILED = "DistributionFailed";

    /**
     * Event type for validation error events from any interface service.
     */
    public static final String VALIDATION_ERROR = "ValidationError";

    // ========== Outbound Event Types ==========

    /**
     * Event type for exception captured events published by this service.
     */
    public static final String EXCEPTION_CAPTURED = "ExceptionCaptured";

    /**
     * Event type for retry completion events published by this service.
     */
    public static final String EXCEPTION_RETRY_COMPLETED = "ExceptionRetryCompleted";

    /**
     * Event type for exception resolution events published by this service.
     */
    public static final String EXCEPTION_RESOLVED = "ExceptionResolved";

    /**
     * Event type for critical exception alert events published by this service.
     */
    public static final String CRITICAL_EXCEPTION_ALERT = "CriticalExceptionAlert";
}