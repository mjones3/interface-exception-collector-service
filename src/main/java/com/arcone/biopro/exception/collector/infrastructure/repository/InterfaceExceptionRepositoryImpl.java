package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom repository implementation using Criteria API for type-safe filtering.
 * This implementation ensures proper parameter type handling and avoids
 * parameter binding issues.
 */
@Repository
public class InterfaceExceptionRepositoryImpl implements InterfaceExceptionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<InterfaceException> findWithFiltersTypeSafe(
            InterfaceType interfaceType,
            ExceptionStatus status,
            ExceptionSeverity severity,
            String customerId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InterfaceException> query = cb.createQuery(InterfaceException.class);
        Root<InterfaceException> root = query.from(InterfaceException.class);

        List<Predicate> predicates = new ArrayList<>();

        // Add filters only if parameters are not null
        if (interfaceType != null) {
            predicates.add(cb.equal(root.get("interfaceType"), interfaceType));
        }

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        if (severity != null) {
            predicates.add(cb.equal(root.get("severity"), severity));
        }

        if (customerId != null && !customerId.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("customerId"), customerId));
        }

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), toDate));
        }

        // Combine all predicates with AND
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Add default ordering by timestamp descending
        query.orderBy(cb.desc(root.get("timestamp")));

        // Create typed query
        TypedQuery<InterfaceException> typedQuery = entityManager.createQuery(query);

        // Apply pagination
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<InterfaceException> results = typedQuery.getResultList();

        // Get total count for pagination
        long total = getTotalCount(interfaceType, status, severity, customerId, fromDate, toDate);

        return new PageImpl<>(results, pageable, total);
    }

    /**
     * Get total count for pagination using the same filters.
     */
    private long getTotalCount(
            InterfaceType interfaceType,
            ExceptionStatus status,
            ExceptionSeverity severity,
            String customerId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<InterfaceException> root = countQuery.from(InterfaceException.class);

        List<Predicate> predicates = new ArrayList<>();

        // Apply the same filters as the main query
        if (interfaceType != null) {
            predicates.add(cb.equal(root.get("interfaceType"), interfaceType));
        }

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        if (severity != null) {
            predicates.add(cb.equal(root.get("severity"), severity));
        }

        if (customerId != null && !customerId.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("customerId"), customerId));
        }

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), toDate));
        }

        countQuery.select(cb.count(root));
        countQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}