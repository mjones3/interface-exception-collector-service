package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseNotificationDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.application.mapper.OrderReceivedEventMapper;
import com.arcone.biopro.distribution.order.application.mapper.PickListCommandMapper;
import com.arcone.biopro.distribution.order.domain.event.OrderCreatedEvent;
import com.arcone.biopro.distribution.order.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.order.domain.model.Order;
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

import java.util.ArrayList;
import java.util.Collections;

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
    public Mono<UseCaseResponseDTO<Order>> findUseCaseResponseById(Long id) {
        return this.orderRepository.findOneById(id)
            .switchIfEmpty(Mono.error(new DomainNotFoundForKeyException(String.format("%s",id))))
            .map(order -> new UseCaseResponseDTO<>(new ArrayList<>(),order))
            .doOnSuccess(this::setAvailableInventories);

    }

    @Override
    public Mono<Order> findOneById(Long id) {
        return this.orderRepository.findOneById(id)
            .switchIfEmpty(Mono.error(new DomainNotFoundForKeyException(String.format("%s",id))));

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

    private void setAvailableInventories(UseCaseResponseDTO<Order> useCaseResponseDTO){
        Flux.from(inventoryService.getAvailableInventories(pickListCommandMapper.mapToDomain(useCaseResponseDTO.data())).onErrorResume(error -> {
                log.error("Not able to fetch inventory Data {}", error.getMessage());
                useCaseResponseDTO.notifications().add(UseCaseNotificationDTO
                    .builder()
                    .notificationType(UseCaseNotificationType.WARN)
                    .notificationMessage("Inventory Data Not Available.")
                    .build());
                return Mono.empty();
            }))
            .flatMap(availableInventory -> {
                    var orderItem = useCaseResponseDTO.data().getOrderItems().stream()
                        .filter(x -> x.getBloodType().getBloodType().equals(availableInventory.getAboRh())
                            && x.getProductFamily().getProductFamily().equals(availableInventory.getProductFamily())).findFirst();
                    orderItem.ifPresent(item -> item.defineAvailableQuantity(availableInventory.getQuantityAvailable()));
                    return Mono.just(availableInventory);
                }
            ).blockLast();
    }
}
