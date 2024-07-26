package com.arcone.biopro.distribution.orderservice.infrastructure.persistence;

import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.orderservice.infrastructure.mapper.OrderEntityMapper;
import com.arcone.biopro.distribution.orderservice.infrastructure.mapper.OrderItemEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final R2dbcEntityTemplate entityTemplate;
    private final OrderEntityMapper orderEntityMapper;
    private final OrderItemEntityMapper orderItemEntityMapper;

    private Query queryById(final Long id, final Boolean active) {
        var criteria = where("id").is(id);

        if (active != null) {
            criteria.and("delete_date").isNull();
        }

        return query(criteria);
    }

    @Override
    public Mono<Boolean> existsById(final Long id) {
        return this.existsById(id, TRUE);
    }

    @Override
    public Mono<Boolean> existsById(final Long id, final Boolean active) {
        return this.entityTemplate
            .select(OrderEntity.class)
            .matching(queryById(id, active))
            .exists();
    }

    private Mono<OrderEntity> findOneEntityById(final Long id) {
        return this.entityTemplate
            .select(OrderEntity.class)
            .matching(queryById(id, TRUE))
            .one();
    }

    private Flux<OrderItemEntity> findAllOrderItemEntitiesByOrderId(final Long orderId) {
        return this.entityTemplate
            .select(OrderItemEntity.class)
            .matching(
                query(where("order_id").is(orderId))
            )
            .all();
    }

    @Override
    public Flux<Order> findAll() {
        return this.entityTemplate
            .select(OrderEntity.class)
            .all()
            .flatMap(orderEntity ->
                findAllOrderItemEntitiesByOrderId(orderEntity.getId())
                    .collect(Collectors.toList())
                    .map(orderItems -> orderEntityMapper.mapToDomain(orderEntity, orderItems))
            );
    }

    @Override
    public Mono<Order> findOneById(final Long id) {
        return this.findOneEntityById(id)
            .flatMap(orderEntity ->
                findAllOrderItemEntitiesByOrderId(orderEntity.getId())
                    .collect(Collectors.toList())
                    .map(orderItems -> orderEntityMapper.mapToDomain(orderEntity, orderItems))
            );
    }

    @Override
    @Transactional
    public Mono<Order> insert(final Order order) {
        return this.entityTemplate
            .insert(orderEntityMapper.mapToEntity(order))
            .flatMap(orderEntity ->
                Flux.fromIterable(order.getOrderItems())
                    .map(orderItemEntityMapper::mapToEntity)
                    .map(orderItem -> orderItem.withOrderId(orderEntity.getId()))
                    .flatMap(this.entityTemplate::insert)
                    .collect(Collectors.toList())
                    .map(orderItemEntities -> orderEntityMapper.mapToDomain(orderEntity, orderItemEntities))
            );
    }

    @Override
    public Mono<Long> countByExternalId(String externalId) {
        return this.entityTemplate
            .select(OrderEntity.class)
            .matching(
                query(where("external_id").is(externalId)
                    .and("delete_date").isNull())
            )
            .count();
    }

}
