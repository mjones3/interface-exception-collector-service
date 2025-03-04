package com.arcone.biopro.distribution.order.infrastructure.persistence;

import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.Page;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderEntityMapper;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderItemEntityMapper;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderReportEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRepositoryImpl implements OrderRepository, FilterAndSortRepository<OrderEntity> {

    private static final String ORDER_PRIORITY_COLOR_TYPE = "ORDER_PRIORITY_COLOR";

    private final R2dbcEntityTemplate entityTemplate;
    private final OrderEntityMapper orderEntityMapper;
    private final OrderItemEntityMapper orderItemEntityMapper;
    private final LookupEntityRepository lookupEntityRepository;
    private final OrderReportEntityMapper orderReportEntityMapper;

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
        return
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

    @Override
    public Mono<Order> reset(Order order) {
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

    @Override
    public Flux<Order> findByExternalId(String externalId) {
        return this.entityTemplate
            .select(OrderEntity.class)
            .matching(query(
                where("externalId").is(externalId)
                    .and("deleteDate").isNull()
            ))
            .all()
            .flatMap(orderEntity ->
                findAllOrderItemEntitiesByOrderId(orderEntity.getId())
                    .collect(Collectors.toList())
                    .flatMap(orderItemEntities -> Mono.fromCallable(()-> orderEntityMapper
                            .mapToDomain(orderEntity, orderItemEntities))
                        .publishOn(Schedulers.boundedElastic()) )
            );
    }

    @Override
    public Mono<Page<OrderReport>> search(OrderQueryCommand orderQueryCommand) {
        var criteria = where("locationCode").is(orderQueryCommand.getLocationCode());
        if (orderQueryCommand.getExternalOrderId() != null && orderQueryCommand.getOrderNumber() != null) {
            criteria = criteria.and(
                where("orderNumber").is(orderQueryCommand.getOrderNumber())
                    .or("externalId").is(orderQueryCommand.getOrderNumber())
            );
        } else if (orderQueryCommand.getExternalOrderId() != null) {
            criteria = criteria.and(where("externalId").is(orderQueryCommand.getExternalOrderId()));
        }
        if (Objects.nonNull(orderQueryCommand.getOrderStatus()) && !orderQueryCommand.getOrderStatus().isEmpty()) {
            criteria = criteria.and(where("status").in(orderQueryCommand.getOrderStatus()));
        }
        if (Objects.nonNull(orderQueryCommand.getDeliveryTypes()) && !orderQueryCommand.getDeliveryTypes().isEmpty()) {
            criteria = criteria.and(where("deliveryType").in(orderQueryCommand.getDeliveryTypes()));
        }
        if (Objects.nonNull(orderQueryCommand.getCustomers()) && !orderQueryCommand.getCustomers().isEmpty()) {
            criteria = criteria.and(where("shippingCustomerCode").in(orderQueryCommand.getCustomers()));
        }
        if (Objects.nonNull(orderQueryCommand.getCreateDateFrom()) && Objects.nonNull(orderQueryCommand.getCreateDateTo())) {
            criteria = criteria.and(
                where("createDate").greaterThanOrEquals(orderQueryCommand.getCreateDateFrom().atStartOfDay())
                    .and("createDate").lessThanOrEquals(orderQueryCommand.getCreateDateTo().atTime(LocalTime.MAX))
            );
        }
        if (Objects.nonNull(orderQueryCommand.getDesireShipDateFrom()) && Objects.nonNull(orderQueryCommand.getDesireShipDateTo())) {
            criteria = criteria.and(
                where("desiredShippingDate").greaterThanOrEquals(orderQueryCommand.getDesireShipDateFrom().atStartOfDay())
                    .and("desiredShippingDate").lessThanOrEquals(orderQueryCommand.getDesireShipDateTo().atTime(LocalTime.MAX))
            );
        }

        var filter = this.filter(OrderEntity.class, entityTemplate, criteria, orderQueryCommand);
        var count = this.count(OrderEntity.class, entityTemplate, criteria);
        var colorMapByPriority = this.fetchOrderColorsMappedByPriority();

        return Flux.zip(filter, colorMapByPriority.cache().repeat())
            .map(tuple -> {
                var orderReport = tuple.getT1();
                var color = tuple.getT2().get(orderReport.getPriority());
                return this.orderReportEntityMapper.mapToDomain(orderReport, color.getOptionValue());
            })
            .collectList()
            .zipWith(count)
            .map(tuple -> this.buildPage(tuple.getT1(), tuple.getT2(), orderQueryCommand));
    }

    private Mono<Map<Integer, LookupEntity>> fetchOrderColorsMappedByPriority() {
        return this.lookupEntityRepository
            .findAllByTypeAndActiveIsTrueOrderByOrderNumberAsc(ORDER_PRIORITY_COLOR_TYPE)
            .collect(Collectors.toMap(LookupEntity::getOrderNumber, Function.identity()));
    }

}
