package com.arcone.biopro.partner.order.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity for tracking all events published by the Partner Order Service.
 * Provides audit trail and debugging capabilities for event publishing.
 */
@Entity
@Table(name = "partner_order_events", indexes = {
        @Index(name = "idx_partner_order_events_event_id", columnList = "eventId"),
        @Index(name = "idx_partner_order_events_transaction_id", columnList = "transactionId"),
        @Index(name = "idx_partner_order_events_event_type", columnList = "eventType"),
        @Index(name = "idx_partner_order_events_published_at", columnList = "publishedAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerOrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "event_version", nullable = false, length = 10)
    @Builder.Default
    private String eventVersion = "1.0";

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "source", nullable = false, length = 100)
    @Builder.Default
    private String source = "partner-order-service";

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(name = "published_at", nullable = false)
    @Builder.Default
    private OffsetDateTime publishedAt = OffsetDateTime.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PartnerOrderEvent))
            return false;
        PartnerOrderEvent that = (PartnerOrderEvent) o;
        return eventId != null && eventId.equals(that.getEventId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PartnerOrderEvent{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", eventVersion='" + eventVersion + '\'' +
                ", transactionId=" + transactionId +
                ", correlationId=" + correlationId +
                ", source='" + source + '\'' +
                ", topic='" + topic + '\'' +
                ", publishedAt=" + publishedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}