package com.arcone.biopro.partner.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Entity representing an individual order line item within a partner order.
 */
@Entity
@Table(name = "partner_order_items", indexes = {
        @Index(name = "idx_partner_order_items_partner_order_id", columnList = "partner_order_id"),
        @Index(name = "idx_partner_order_items_product_family", columnList = "productFamily"),
        @Index(name = "idx_partner_order_items_blood_type", columnList = "bloodType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_order_id", nullable = false)
    private PartnerOrder partnerOrder;

    @NotBlank(message = "Product family is required")
    @Column(name = "product_family", nullable = false, length = 255)
    private String productFamily;

    @NotBlank(message = "Blood type is required")
    @Column(name = "blood_type", nullable = false, length = 50)
    private String bloodType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PartnerOrderItem))
            return false;
        PartnerOrderItem that = (PartnerOrderItem) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PartnerOrderItem{" +
                "id=" + id +
                ", productFamily='" + productFamily + '\'' +
                ", bloodType='" + bloodType + '\'' +
                ", quantity=" + quantity +
                ", comments='" + comments + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}