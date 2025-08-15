package com.arcone.biopro.exception.collector.domain.event.constants;

/**
 * Constants for Kafka topic names used by the Interface Exception Collector
 * Service.
 * Provides centralized definition of topic names for consumers and producers.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Utility class - prevent instantiation
    }

    // ========== Inbound Topics (Consumed) ==========

    /**
     * Topic for order rejection events from Order Service.
     */
    public static final String ORDER_REJECTED = "OrderRejected";

    /**
     * Topic for order cancellation events from Order Service.
     */
    public static final String ORDER_CANCELLED = "OrderCancelled";

    /**
     * Topic for collection rejection events from Collection Service.
     */
    public static final String COLLECTION_REJECTED = "CollectionRejected";

    /**
     * Topic for distribution failure events from Distribution Service.
     */
    public static final String DISTRIBUTION_FAILED = "DistributionFailed";

    /**
     * Topic for validation error events from any interface service.
     */
    public static final String VALIDATION_ERROR = "ValidationError";

    // ========== Outbound Topics (Published) ==========

    /**
     * Topic for exception captured events published by this service.
     */
    public static final String EXCEPTION_CAPTURED = "ExceptionCaptured";

    /**
     * Topic for retry completion events published by this service.
     */
    public static final String EXCEPTION_RETRY_COMPLETED = "ExceptionRetryCompleted";

    /**
     * Topic for exception resolution events published by this service.
     */
    public static final String EXCEPTION_RESOLVED = "ExceptionResolved";

    /**
     * Topic for critical exception alert events published by this service.
     */
    public static final String CRITICAL_EXCEPTION_ALERT = "CriticalExceptionAlert";
}