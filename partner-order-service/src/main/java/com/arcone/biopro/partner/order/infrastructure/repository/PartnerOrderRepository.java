package com.arcone.biopro.partner.order.infrastructure.repository;

import com.arcone.biopro.partner.order.domain.entity.PartnerOrder;
import com.arcone.biopro.partner.order.domain.enums.OrderStatus;
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
 * Repository interface for PartnerOrder entities.
 * Provides data access methods for order management and retrieval.
 */
@Repository
public interface PartnerOrderRepository extends JpaRepository<PartnerOrder, Long> {

    /**
     * Finds a partner order by its transaction ID.
     */
    Optional<PartnerOrder> findByTransactionId(UUID transactionId);

    /**
     * Finds a partner order by its external ID.
     */
    Optional<PartnerOrder> findByExternalId(String externalId);

    /**
     * Checks if an order with the given external ID exists.
     */
    boolean existsByExternalId(String externalId);

    /**
     * Finds orders by status.
     */
    List<PartnerOrder> findByStatus(OrderStatus status);

    /**
     * Finds orders by location code.
     */
    List<PartnerOrder> findByLocationCode(String locationCode);

    /**
     * Finds orders by product category.
     */
    List<PartnerOrder> findByProductCategory(String productCategory);

    /**
     * Finds orders submitted within a date range.
     */
    List<PartnerOrder> findBySubmittedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate);

    /**
     * Finds orders by status and location code.
     */
    List<PartnerOrder> findByStatusAndLocationCode(OrderStatus status, String locationCode);

    /**
     * Finds orders with pagination and sorting.
     */
    Page<PartnerOrder> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Finds orders by location code with pagination.
     */
    Page<PartnerOrder> findByLocationCode(String locationCode, Pageable pageable);

    /**
     * Custom query to find orders with specific criteria.
     */
    @Query("SELECT po FROM PartnerOrder po WHERE " +
            "(:status IS NULL OR po.status = :status) AND " +
            "(:locationCode IS NULL OR po.locationCode = :locationCode) AND " +
            "(:productCategory IS NULL OR po.productCategory = :productCategory) AND " +
            "(:startDate IS NULL OR po.submittedAt >= :startDate) AND " +
            "(:endDate IS NULL OR po.submittedAt <= :endDate)")
    Page<PartnerOrder> findOrdersWithFilters(
            @Param("status") OrderStatus status,
            @Param("locationCode") String locationCode,
            @Param("productCategory") String productCategory,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Counts orders by status.
     */
    long countByStatus(OrderStatus status);

    /**
     * Counts orders by location code.
     */
    long countByLocationCode(String locationCode);

    /**
     * Finds recent orders for a specific location.
     */
    @Query("SELECT po FROM PartnerOrder po WHERE po.locationCode = :locationCode " +
            "ORDER BY po.submittedAt DESC")
    List<PartnerOrder> findRecentOrdersByLocation(@Param("locationCode") String locationCode,
            Pageable pageable);

    /**
     * Finds orders that need processing (status = RECEIVED).
     */
    @Query("SELECT po FROM PartnerOrder po WHERE po.status = 'RECEIVED' " +
            "ORDER BY po.submittedAt ASC")
    List<PartnerOrder> findOrdersToProcess(Pageable pageable);

    /**
     * Finds orders submitted in the last N hours.
     */
    @Query("SELECT po FROM PartnerOrder po WHERE po.submittedAt >= :since " +
            "ORDER BY po.submittedAt DESC")
    List<PartnerOrder> findRecentOrders(@Param("since") OffsetDateTime since);

    /**
     * Custom query to search orders by external ID pattern.
     */
    @Query("SELECT po FROM PartnerOrder po WHERE po.externalId LIKE %:pattern%")
    List<PartnerOrder> findByExternalIdContaining(@Param("pattern") String pattern);
}