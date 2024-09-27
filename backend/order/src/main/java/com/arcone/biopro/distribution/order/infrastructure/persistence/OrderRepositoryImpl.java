package com.arcone.biopro.distribution.order.infrastructure.persistence;

import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderEntityMapper;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderItemEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Component
@RequiredArgsConstructor
@Slf4j
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
    public Mono<Order> findOneById(final Long id) {
        return this.findOneEntityById(id)
            .flatMap(orderEntity ->
                findAllOrderItemEntitiesByOrderId(orderEntity.getId())
                    .collect(Collectors.toList())
                    .flatMap(orderItemEntities -> Mono.fromCallable(()-> orderEntityMapper
                        .mapToDomain(orderEntity, orderItemEntities))
                        .publishOn(Schedulers.boundedElastic()) )
            );
    }

    @Override
    @Transactional
    public Mono<Order> insert(final Order order) {
        log.info("Inserting order: {}", order);
        return this.entityTemplate
            .insert(orderEntityMapper.mapToEntity(order))
            .flatMap(orderEntity ->
                Flux.fromIterable(order.getOrderItems())
                    .map(orderItemEntityMapper::mapToEntity)
                    .map(orderItem -> orderItem.withOrderId(orderEntity.getId()))
                    .flatMap(this.entityTemplate::insert)
                    .collect(Collectors.toList())
                    .flatMap(orderItemEntities -> Mono.fromCallable(()-> orderEntityMapper
                        .mapToDomain(orderEntity, orderItemEntities))
                        .publishOn(Schedulers.boundedElastic()) )
            );
    }

    @Override
    public Mono<Order> findOneByOrderNumber(Long number) {
        return this.entityTemplate
            .select(OrderEntity.class)
            .matching(query(
                where("orderNumber").is(number)
                    .and("deleteDate").isNull()
            ))
            .one()
            .flatMap(orderEntity ->
                findAllOrderItemEntitiesByOrderId(orderEntity.getId())
                    .collect(Collectors.toList())
                    .flatMap(orderItemEntities -> Mono.fromCallable(()-> orderEntityMapper
                            .mapToDomain(orderEntity, orderItemEntities))
                        .publishOn(Schedulers.boundedElastic()) )
            );
    }

    @Override
    public Mono<Order> update(Order order) {
        return this.entityTemplate
            .update(orderEntityMapper.mapToEntity(order))
            .flatMap(orderEntity ->
                Flux.fromIterable(order.getOrderItems())
                    .map(orderItemEntityMapper::mapToEntity)
                    .map(orderItem -> orderItem.withOrderId(orderEntity.getId()))
                    .flatMap(this.entityTemplate::update)
                    .collect(Collectors.toList())
                    .flatMap(orderItemEntities -> Mono.fromCallable(()-> orderEntityMapper
                            .mapToDomain(orderEntity, orderItemEntities))
                        .publishOn(Schedulers.boundedElastic()) )
            );
    }


}
