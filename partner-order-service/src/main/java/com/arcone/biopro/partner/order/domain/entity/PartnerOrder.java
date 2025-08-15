package com.arcone.biopro.partner.order.domain.entity;

import com.arcone.biopro.partner.order.domain.enums.OrderStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a partner order submission.
 * Stores the complete original payload and metadata for retry operations.
 */
@Entity
@Table(name = "partner_orders", indexes = {
        @Index(name = "idx_partner_orders_transaction_id", columnList = "transactionId"),
        @Index(name = "idx_partner_orders_external_id", columnList = "externalId"),
        @Index(name = "idx_partner_orders_status", columnList = "status"),
        @Index(name = "idx_partner_orders_submitted_at", columnList = "submittedAt"),
        @Index(name = "idx_partner_orders_location_code", columnList = "locationCode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private UUID transactionId;

    @Column(name = "external_id", nullable = false, unique = true, length = 255)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private OrderStatus status = OrderStatus.RECEIVED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "original_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode originalPayload;

    @Column(name = "location_code", length = 255)
    private String locationCode;

    @Column(name = "product_category", length = 255)
    private String productCategory;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private OffsetDateTime submittedAt = OffsetDateTime.now();

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "partnerOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PartnerOrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "transactionId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PartnerOrderEvent> events = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    /**
     * Adds an order item to this partner order.
     */
    public void addOrderItem(PartnerOrderItem item) {
        orderItems.add(item);
        item.setPartnerOrder(this);
    }

    /**
     * Removes an order item from this partner order.
     */
    public void removeOrderItem(PartnerOrderItem item) {
        orderItems.remove(item);
        item.setPartnerOrder(null);
    }

    /**
     * Marks the order as processed.
     */
    public void markAsProcessed() {
        this.processedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Updates the order status.
     */
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = OffsetDateTime.now();
    }
}