package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.FilterAndSortCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Page;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.QueryOrderBy;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.QuerySort;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.springframework.data.relational.core.query.Query.query;

public interface FilterAndSortRepository<E> {

    private Sort compileQuerySort(QuerySort sort) {
        var sortingRules = ofNullable(sort)
            .map(QuerySort::getQueryOrderByList)
            .orElse(Collections.emptyList()).stream()
            .map(this::toSortOrder)
            .toList();

        return Sort.by(sortingRules);
    }

    private Sort.Order toSortOrder(QueryOrderBy orderBy) {
        return new Sort.Order(Sort.Direction.fromString(orderBy.getDirection()), orderBy.getProperty());
    }

    default Flux<E> filter(Class<E> entityType, R2dbcEntityTemplate entityTemplate, Criteria criteria, FilterAndSortCommand filterAndSortCommand) {
        var sort = compileQuerySort(filterAndSortCommand.getQuerySort());
        var offset = filterAndSortCommand.getPageNumber().longValue() * filterAndSortCommand.getPageSize().longValue();
        return entityTemplate
            .select(entityType)
            .matching(
                query(criteria)
                    .sort(sort)
                    .limit(filterAndSortCommand.getPageSize())
                    .offset(offset)
            )
            .all();
    }

    default Mono<Long> count(Class<E> entityType, R2dbcEntityTemplate entityTemplate, Criteria criteria) {
        return entityTemplate
            .count(query(criteria), entityType);
    }

    default Mono<Page<E>> page(Class<E> entityType, R2dbcEntityTemplate entityTemplate, Criteria criteria, FilterAndSortCommand filterAndSortCommand) {
        var filter = this.filter(entityType, entityTemplate, criteria, filterAndSortCommand).collectList();
        var count = this.count(entityType, entityTemplate, criteria);
        return Mono.zip(filter, count)
            .map(tuple -> this.buildPage(tuple.getT1(), tuple.getT2(), filterAndSortCommand));
    }

    default <T> Page<T> buildPage(List<T> content, Long count, FilterAndSortCommand filterAndSortCommand) {
        return new Page<>(
            content,
            filterAndSortCommand.getPageNumber(),
            filterAndSortCommand.getPageSize(),
            count,
            filterAndSortCommand.getQuerySort()
        );
    }

}
