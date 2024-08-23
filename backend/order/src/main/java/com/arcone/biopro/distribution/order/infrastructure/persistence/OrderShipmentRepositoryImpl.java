package com.arcone.biopro.distribution.order.infrastructure.persistence;

import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import com.arcone.biopro.distribution.order.domain.repository.OrderShipmentRepository;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderShipmentEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderShipmentRepositoryImpl implements OrderShipmentRepository {

    private final R2dbcEntityTemplate entityTemplate;
    private final OrderShipmentEntityMapper orderShipmentEntityMapper;

    @Override
    public Mono<OrderShipment> insert(OrderShipment orderShipment) {
        return this.entityTemplate
            .insert(orderShipmentEntityMapper.mapToEntity(orderShipment))
            .map(orderShipmentEntityMapper::mapToDomain);
    }
}
