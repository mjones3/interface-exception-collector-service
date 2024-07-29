package com.arcone.biopro.distribution.orderservice.application.usecase;

import com.arcone.biopro.distribution.orderservice.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.orderservice.application.mapper.OrderReceivedEventMapper;
import com.arcone.biopro.distribution.orderservice.domain.event.OrderCreatedEvent;
import com.arcone.biopro.distribution.orderservice.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderUseCase implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderReceivedEventMapper orderReceivedEventMapper;

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

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public Mono<Order> processOrder(OrderReceivedEventPayloadDTO eventDTO) {
        log.info("Processing Order Received Event {}", eventDTO);
        try{
            return orderReceivedEventMapper.mapToDomain(eventDTO)
                .doOnNext(this::insert)
                .doOnSuccess(this::publishOrderCreatedEvent)
                .onErrorResume(error -> {
                        if(error instanceof DuplicateKeyException) {
                            publishOrderRejectedEvent(eventDTO.externalId(),"Order already exists");
                        }else{
                            publishOrderRejectedEvent(eventDTO.externalId(),error.getMessage());
                        }
                        return Mono.error(new RuntimeException("Error processing Order Received Event", error));
                    }
                ).log();
        }catch (Exception e ){
            publishOrderRejectedEvent(eventDTO.externalId(),e.getMessage());
            return Mono.error(new RuntimeException("Error processing Order Received Event", e));
        }
    }

    private void publishOrderCreatedEvent(Order order) {
        applicationEventPublisher.publishEvent(new OrderCreatedEvent(order));
    }

    private void publishOrderRejectedEvent(String externalId,String errorMessage) {
        applicationEventPublisher.publishEvent(new OrderRejectedEvent(externalId,errorMessage));
    }

}
