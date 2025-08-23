package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of custom repository methods for InterfaceException.
 * Uses JPA Criteria API for type-safe dynamic queries.
 */
@Repository
@Slf4j
public class InterfaceExceptionRepositoryImpl implements InterfaceExceptionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<InterfaceException> findWithFiltersTypeSafe(
            InterfaceType interfaceType,
            ExceptionStatus status,
            ExceptionSeverity severity,
            String customerId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            Sort sort) {

        log.debug(
                "Finding exceptions with type-safe filters: interfaceType={}, status={}, severity={}, customerId={}, fromDate={}, toDate={}",
                interfaceType, status, severity, customerId, fromDate, toDate);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InterfaceException> query = cb.createQuery(InterfaceException.class);
        Root<InterfaceException> root = query.from(InterfaceException.class);

        // Build predicates
        List<Predicate> predicates = new ArrayList<>();

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

        // Apply predicates
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Apply sorting
        if (sort != null && sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : sort) {
                String property = sortOrder.getProperty();
                if (sortOrder.isAscending()) {
                    orders.add(cb.asc(root.get(property)));
                } else {
                    orders.add(cb.desc(root.get(property)));
                }
            }
            query.orderBy(orders);
        } else {
            // Default sorting by timestamp descending
            query.orderBy(cb.desc(root.get("timestamp")));
        }

        TypedQuery<InterfaceException> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    @Override
    public Page<InterfaceException> findWithFiltersTypeSafePageable(
            InterfaceType interfaceType,
            ExceptionStatus status,
            ExceptionSeverity severity,
            String customerId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            Pageable pageable) {

        log.debug(
                "Finding exceptions with type-safe filters (pageable): interfaceType={}, status={}, severity={}, customerId={}, fromDate={}, toDate={}",
                interfaceType, status, severity, customerId, fromDate, toDate);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InterfaceException> query = cb.createQuery(InterfaceException.class);
        Root<InterfaceException> root = query.from(InterfaceException.class);

        // Build predicates
        List<Predicate> predicates = new ArrayList<>();

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

        // Apply predicates
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Apply sorting from pageable
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : pageable.getSort()) {
                String property = sortOrder.getProperty();
                if (sortOrder.isAscending()) {
                    orders.add(cb.asc(root.get(property)));
                } else {
                    orders.add(cb.desc(root.get(property)));
                }
            }
            query.orderBy(orders);
        } else {
            // Default sorting by timestamp descending
            query.orderBy(cb.desc(root.get("timestamp")));
        }

        // Execute query with pagination
        TypedQuery<InterfaceException> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<InterfaceException> content = typedQuery.getResultList();

        // Get total count for pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<InterfaceException> countRoot = countQuery.from(InterfaceException.class);
        countQuery.select(cb.count(countRoot));

        // Apply same predicates to count query
        if (!predicates.isEmpty()) {
            // Rebuild predicates for count query with the new root
            List<Predicate> countPredicates = new ArrayList<>();

            if (interfaceType != null) {
                countPredicates.add(cb.equal(countRoot.get("interfaceType"), interfaceType));
            }

            if (status != null) {
                countPredicates.add(cb.equal(countRoot.get("status"), status));
            }

            if (severity != null) {
                countPredicates.add(cb.equal(countRoot.get("severity"), severity));
            }

            if (customerId != null && !customerId.trim().isEmpty()) {
                countPredicates.add(cb.equal(countRoot.get("customerId"), customerId));
            }

            if (fromDate != null) {
                countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("timestamp"), fromDate));
            }

            if (toDate != null) {
                countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("timestamp"), toDate));
            }

            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, totalElements);
    }

    @Override
    public List<InterfaceException> searchInFields(
            String searchQuery,
            List<String> searchFields,
            Sort sort) {

        log.debug("Searching exceptions in fields: query='{}', fields={}", searchQuery, searchFields);

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InterfaceException> query = cb.createQuery(InterfaceException.class);
        Root<InterfaceException> root = query.from(InterfaceException.class);

        // Build search predicates
        List<Predicate> searchPredicates = new ArrayList<>();
        String searchPattern = "%" + searchQuery.toLowerCase() + "%";

        for (String field : searchFields) {
            switch (field) {
                case "exceptionReason":
                    searchPredicates.add(cb.like(cb.lower(root.get("exceptionReason")), searchPattern));
                    break;
                case "externalId":
                    searchPredicates.add(cb.and(
                            cb.isNotNull(root.get("externalId")),
                            cb.like(cb.lower(root.get("externalId")), searchPattern)));
                    break;
                case "operation":
                    searchPredicates.add(cb.like(cb.lower(root.get("operation")), searchPattern));
                    break;
                default:
                    log.warn("Unknown search field: {}", field);
                    break;
            }
        }

        if (!searchPredicates.isEmpty()) {
            query.where(cb.or(searchPredicates.toArray(new Predicate[0])));
        }

        // Apply sorting
        if (sort != null && sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : sort) {
                String property = sortOrder.getProperty();
                if (sortOrder.isAscending()) {
                    orders.add(cb.asc(root.get(property)));
                } else {
                    orders.add(cb.desc(root.get(property)));
                }
            }
            query.orderBy(orders);
        } else {
            // Default sorting by timestamp descending
            query.orderBy(cb.desc(root.get("timestamp")));
        }

        TypedQuery<InterfaceException> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    @Override
    public List<InterfaceException> findRelatedExceptionsByCustomer(
            String customerId,
            String excludeTransactionId,
            Sort sort,
            int limit) {

        log.debug("Finding related exceptions for customer: {}, excluding: {}, limit: {}",
                customerId, excludeTransactionId, limit);

        if (customerId == null || customerId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InterfaceException> query = cb.createQuery(InterfaceException.class);
        Root<InterfaceException> root = query.from(InterfaceException.class);

        // Build predicates
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("customerId"), customerId));

        if (excludeTransactionId != null && !excludeTransactionId.trim().isEmpty()) {
            predicates.add(cb.notEqual(root.get("transactionId"), excludeTransactionId));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Apply sorting
        if (sort != null && sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : sort) {
                String property = sortOrder.getProperty();
                if (sortOrder.isAscending()) {
                    orders.add(cb.asc(root.get(property)));
                } else {
                    orders.add(cb.desc(root.get(property)));
                }
            }
            query.orderBy(orders);
        } else {
            // Default sorting by timestamp descending
            query.orderBy(cb.desc(root.get("timestamp")));
        }

        TypedQuery<InterfaceException> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(limit);
        return typedQuery.getResultList();
    }
}