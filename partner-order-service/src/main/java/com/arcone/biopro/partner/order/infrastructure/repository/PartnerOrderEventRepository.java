package com.arcone.biopro.partner.order.infrastructure.repository;

import com.arcone.biopro.partner.order.domain.entity.PartnerOrderEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PartnerOrderEvent entities.
 * Provides data access methods for event tracking and audit trail.
 */
@Repository
public interface PartnerOrderEventRepository extends JpaRepository<PartnerOrderEvent, Long> {

    /**
     * Finds an event by its event ID.
     */
    Optional<PartnerOrderEvent> findByEventId(UUID eventId);

    /**
     * Finds all events for a specific transaction ID.
     */
    List<PartnerOrderEvent> findByTransactionIdOrderByPublishedAtDesc(UUID transactionId);

    /**
     * Finds events by event type.
     */
    List<PartnerOrderEvent> findByEventType(String eventType);

    /**
     * Finds events by topic.
     */
    List<PartnerOrderEvent> findByTopic(String topic);

    /**
     * Finds events by correlation ID.
     */
    List<PartnerOrderEvent> findByCorrelationId(UUID correlationId);

    /**
     * Finds events published within a date range.
     */
    List<PartnerOrderEvent> findByPublishedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate);

    /**
     * Finds events by event type with pagination.
     */
    Page<PartnerOrderEvent> findByEventType(String eventType, Pageable pageable);

    /**
     * Finds events by topic with pagination.
     */
    Page<PartnerOrderEvent> findByTopic(String topic, Pageable pageable);

    /**
     * Custom query to find events with filters.
     */
    @Query("SELECT poe FROM PartnerOrderEvent poe WHERE " +
            "(:eventType IS NULL OR poe.eventType = :eventType) AND " +
            "(:topic IS NULL OR poe.topic = :topic) AND " +
            "(:transactionId IS NULL OR poe.transactionId = :transactionId) AND " +
            "(:startDate IS NULL OR poe.publishedAt >= :startDate) AND " +
            "(:endDate IS NULL OR poe.publishedAt <= :endDate) " +
            "ORDER BY poe.publishedAt DESC")
    Page<PartnerOrderEvent> findEventsWithFilters(
            @Param("eventType") String eventType,
            @Param("topic") String topic,
            @Param("transactionId") UUID transactionId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Counts events by event type.
     */
    long countByEventType(String eventType);

    /**
     * Counts events by topic.
     */
    long countByTopic(String topic);

    /**
     * Finds recent events for debugging.
     */
    @Query("SELECT poe FROM PartnerOrderEvent poe WHERE poe.publishedAt >= :since " +
            "ORDER BY poe.publishedAt DESC")
    List<PartnerOrderEvent> findRecentEvents(@Param("since") OffsetDateTime since);

    /**
     * Finds events by transaction ID with pagination.
     */
    Page<PartnerOrderEvent> findByTransactionId(UUID transactionId, Pageable pageable);

    /**
     * Finds the latest event for a transaction ID.
     */
    @Query("SELECT poe FROM PartnerOrderEvent poe WHERE poe.transactionId = :transactionId " +
            "ORDER BY poe.publishedAt DESC")
    Optional<PartnerOrderEvent> findLatestEventByTransactionId(@Param("transactionId") UUID transactionId);

    /**
     * Finds events by event type and date range for reporting.
     */
    @Query("SELECT poe FROM PartnerOrderEvent poe WHERE poe.eventType = :eventType " +
            "AND poe.publishedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY poe.publishedAt DESC")
    List<PartnerOrderEvent> findEventsByTypeAndDateRange(
            @Param("eventType") String eventType,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    /**
     * Checks if an event with the given event ID exists.
     */
    boolean existsByEventId(UUID eventId);
}