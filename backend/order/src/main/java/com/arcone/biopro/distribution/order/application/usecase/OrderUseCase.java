package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.application.mapper.OrderReceivedEventMapper;
import com.arcone.biopro.distribution.order.application.mapper.PickListCommandMapper;
import com.arcone.biopro.distribution.order.domain.event.OrderCreatedEvent;
import com.arcone.biopro.distribution.order.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.PickListItemShortDate;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.InventoryService;
import com.arcone.biopro.distribution.order.domain.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderUseCase implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderReceivedEventMapper orderReceivedEventMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final InventoryService inventoryService;
    private final PickListCommandMapper pickListCommandMapper;

    @Override
    public Mono<Order> findOneById(Long id) {
        return this.orderRepository.findOneById(id)
            .switchIfEmpty(Mono.error(new DomainNotFoundForKeyException(String.format("%s",id))))
            .doOnSuccess(this::setAvailableInventories);
    }

    @Override
    @Transactional
    public Mono<Order> processOrder(OrderReceivedEventPayloadDTO eventDTO) {
        log.info("Processing Order Received Event {}", eventDTO);
        try{
            return orderReceivedEventMapper.mapToDomain(eventDTO)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(order -> {
                        log.info("Result orderReceivedEventMapper.mapToDomain {} , ID {}", order, order.getId());
                        this.orderRepository.insert(order)
                            .doOnSuccess(this::publishOrderCreatedEvent)
                            .doOnError(error -> {
                                if(error instanceof DuplicateKeyException) {
                                    publishOrderRejectedEvent(eventDTO.externalId(),"Order already exists");
                                }else{
                                    publishOrderRejectedEvent(eventDTO.externalId(),error.getMessage());
                                }
                            })
                            .subscribe();
                })
                .onErrorResume(error -> {
                        if(error instanceof DuplicateKeyException) {
                            publishOrderRejectedEvent(eventDTO.externalId(),"Order already exists");
                        }else{
                            publishOrderRejectedEvent(eventDTO.externalId(),error.getMessage());
                        }
                        return Mono.error(new RuntimeException("Error processing Order Received Event", error));
                    }
                );
        }catch (Exception e ){
            publishOrderRejectedEvent(eventDTO.externalId(),e.getMessage());
            return Mono.error(new RuntimeException("Error processing Order Received Event", e));
        }
    }

    private void publishOrderCreatedEvent(Order order) {
        log.debug("Publishing OrderCreatedEvent {} , ID {}", order, order.getId());
        applicationEventPublisher.publishEvent(new OrderCreatedEvent(order));
    }

    private void publishOrderRejectedEvent(String externalId,String errorMessage) {
        log.debug("Publishing OrderRejected {} , ID {}", externalId, errorMessage);
        applicationEventPublisher.publishEvent(new OrderRejectedEvent(externalId,errorMessage));
    }

    private void setAvailableInventories(Order order){
        Flux.from(inventoryService.getAvailableInventories(pickListCommandMapper.mapToDomain(order)))
            .flatMap(availableInventory -> {
                    var orderItem = order.getOrderItems().stream()
                        .filter(x -> x.getBloodType().getBloodType().equals(availableInventory.getAboRh())
                            && x.getProductFamily().getProductFamily().equals(availableInventory.getProductFamily())).findFirst();
                    orderItem.ifPresent(item -> item.defineAvailableQuantity(availableInventory.getQuantityAvailable()));
                    return Mono.just(availableInventory);
                }
            ).blockLast();
    }
}
