package com.arcone.biopro.exception.collector.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

/**
 * JPA Entity representing an order item from OrderRejected events.
 * Contains blood type, product family, and quantity information.
 */
@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_interface_exception_id", columnList = "interfaceExceptionId"),
        @Index(name = "idx_order_items_blood_type", columnList = "bloodType"),
        @Index(name = "idx_order_items_product_family", columnList = "productFamily")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "interfaceException")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Interface exception is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_exception_id", nullable = false)
    @JsonBackReference("exception-orderItems")
    private InterfaceException interfaceException;

    @NotBlank(message = "Blood type is required")
    @Size(max = 10, message = "Blood type must not exceed 10 characters")
    @Column(name = "blood_type", nullable = false, length = 10)
    private String bloodType;

    @NotBlank(message = "Product family is required")
    @Size(max = 50, message = "Product family must not exceed 50 characters")
    @Column(name = "product_family", nullable = false, length = 50)
    private String productFamily;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Convenience method to set the interface exception and maintain bidirectional
     * relationship.
     */
    public void setInterfaceException(InterfaceException interfaceException) {
        this.interfaceException = interfaceException;
        if (interfaceException != null && !interfaceException.getOrderItems().contains(this)) {
            interfaceException.getOrderItems().add(this);
        }
    }
}