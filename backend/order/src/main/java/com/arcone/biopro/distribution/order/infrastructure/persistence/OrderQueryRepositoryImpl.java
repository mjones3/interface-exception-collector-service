package com.arcone.biopro.distribution.order.infrastructure.persistence;

import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.repository.OrderQueryRepository;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderReportEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final R2dbcEntityTemplate entityTemplate;
    private final OrderReportEntityMapper orderReportEntityMapper;
    private static final String ORDER_PRIORITY_COLOR_TYPE = "ORDER_PRIORITY_COLOR";

    @Override
    public Flux<OrderReport> searchOrders(OrderQueryCommand orderQueryCommand) {
        var criteria = where("locationCode").is(orderQueryCommand.getLocationCode());
        var sort = orderQueryCommand.getQuerySort()
            .getQueryOrderByList().stream()
            .map(orderBy -> new Sort.Order(Sort.Direction.fromString(orderBy.getDirection()), orderBy.getProperty()))
            .toList();

        return entityTemplate.select(OrderEntity.class)
                    .matching(Query.query(criteria)
                        .sort(Sort.by(sort))
                        .limit(orderQueryCommand.getLimit())
                    ).all().flatMap(orderEntity -> findPriorityColor(orderEntity.getPriority())
                .map(lookup -> orderReportEntityMapper.mapToDomain(orderEntity,lookup.getOptionValue()))
            );


    }

    private Mono<LookupEntity> findPriorityColor(final String priority){
        return this.entityTemplate.select(LookupEntity.class)
            .matching(Query.query(
                where("type").is(ORDER_PRIORITY_COLOR_TYPE)
                    .and("active").is(Boolean.TRUE)
                    .and("descriptionKey").is(priority)
            )).one();
    }
}
