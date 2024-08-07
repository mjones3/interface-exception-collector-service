package com.arcone.biopro.distribution.order.infrastructure.persistence;

import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.repository.OrderQueryRepository;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderReportEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private static final String ORDER_PRIORITY_COLOR_TYPE = "ORDER_PRIORITY_COLOR";

    private final R2dbcEntityTemplate entityTemplate;
    private final OrderReportEntityMapper orderReportEntityMapper;

    @Override
    public Flux<OrderReport> searchOrders(OrderQueryCommand orderQueryCommand) {
        var criteria = where("locationCode").is(orderQueryCommand.getLocationCode());
        var sorts = orderQueryCommand.getQuerySort()
            .getQueryOrderByList().stream()
            .map(orderBy -> new Sort.Order(Sort.Direction.fromString(orderBy.getDirection()), orderBy.getProperty()))
            .toList();

        var orders = queryOrdersByCriteria(criteria, Sort.by(sorts), orderQueryCommand.getLimit());
        var priorityMap = queryLookupsByTypeToMapByOrderNumber(ORDER_PRIORITY_COLOR_TYPE);
        return Flux.zip(orders, priorityMap.cache().repeat())
            .map(tuple -> {
                var orderReport = tuple.getT1();
                var orderPriorityLookupColorMap = tuple.getT2();
                var colorLookupEntry = orderPriorityLookupColorMap.get(orderReport.getPriority());
                return orderReportEntityMapper.mapToDomain(orderReport, colorLookupEntry.getOptionValue());
            });
    }

    private Flux<OrderEntity> queryOrdersByCriteria(Criteria criteria, Sort sort, int limit) {
        return entityTemplate.select(OrderEntity.class)
            .matching(
                query(criteria)
                    .sort(sort)
                    .limit(limit)
            )
            .all();
    }

    private Mono<Map<Integer, LookupEntity>> queryLookupsByTypeToMapByOrderNumber(String type) {
        return entityTemplate.select(LookupEntity.class)
            .matching(query(
                where("type").is(type)
                    .and("active").is(TRUE)
            ))
            .all()
            .collect(Collectors.toMap(LookupEntity::getOrderNumber, Function.identity()));
    }

}
