package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderUseCase implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Flux<Order> findAll() {
        return this.orderRepository.findAll();
    }

    @Override
    public Mono<Order> findOneById(Long id) {
        return this.orderRepository.findOneById(id);
    }

    @Override
    public Mono<Order> insert(Order order) {
        return this.orderRepository.insert(order);
    }

}
