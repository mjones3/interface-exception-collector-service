package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity providing data access operations.
 * Supports queries for blood type and product family analysis.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items for a specific interface exception.
     * 
     * @param interfaceExceptionId the interface exception ID
     * @return list of order items
     */
    List<OrderItem> findByInterfaceExceptionId(Long interfaceExceptionId);

    /**
     * Find order items by blood type with pagination.
     * 
     * @param bloodType the blood type to filter by
     * @param pageable  pagination and sorting parameters
     * @return page of order items matching the blood type
     */
    Page<OrderItem> findByBloodType(String bloodType, Pageable pageable);

    /**
     * Find order items by product family with pagination.
     * 
     * @param productFamily the product family to filter by
     * @param pageable      pagination and sorting parameters
     * @return page of order items matching the product family
     */
    Page<OrderItem> findByProductFamily(String productFamily, Pageable pageable);

    /**
     * Find order items by blood type and product family.
     * 
     * @param bloodType     the blood type to filter by
     * @param productFamily the product family to filter by
     * @param pageable      pagination and sorting parameters
     * @return page of order items matching both criteria
     */
    Page<OrderItem> findByBloodTypeAndProductFamily(String bloodType, String productFamily, Pageable pageable);

    /**
     * Get aggregated quantity by blood type for reporting.
     * 
     * @return list of objects containing blood type and total quantity
     */
    @Query("SELECT oi.bloodType, SUM(oi.quantity) as totalQuantity " +
            "FROM OrderItem oi " +
            "GROUP BY oi.bloodType " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> getQuantityByBloodType();

    /**
     * Get aggregated quantity by product family for reporting.
     * 
     * @return list of objects containing product family and total quantity
     */
    @Query("SELECT oi.productFamily, SUM(oi.quantity) as totalQuantity " +
            "FROM OrderItem oi " +
            "GROUP BY oi.productFamily " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> getQuantityByProductFamily();

    /**
     * Find order items with high quantities (potential bulk orders).
     * 
     * @param minQuantity minimum quantity threshold
     * @param pageable    pagination and sorting parameters
     * @return page of order items with quantity >= minQuantity
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.quantity >= :minQuantity ORDER BY oi.quantity DESC")
    Page<OrderItem> findHighQuantityItems(@Param("minQuantity") Integer minQuantity, Pageable pageable);

    /**
     * Count total order items for a specific blood type.
     * 
     * @param bloodType the blood type
     * @return count of order items
     */
    long countByBloodType(String bloodType);

    /**
     * Count total order items for a specific product family.
     * 
     * @param productFamily the product family
     * @return count of order items
     */
    long countByProductFamily(String productFamily);
}