package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.mapper.PickListCommandMapper;
import com.arcone.biopro.distribution.order.application.mapper.PickListMapper;
import com.arcone.biopro.distribution.order.domain.event.PickListCreatedEvent;
import com.arcone.biopro.distribution.order.domain.model.GeneratePickListCommand;
import com.arcone.biopro.distribution.order.domain.model.GeneratePickListProductCriteria;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.domain.model.PickListItem;
import com.arcone.biopro.distribution.order.domain.model.PickListItemShortDate;
import com.arcone.biopro.distribution.order.domain.model.vo.PickListCustomer;
import com.arcone.biopro.distribution.order.domain.service.InventoryService;
import com.arcone.biopro.distribution.order.domain.service.OrderService;
import com.arcone.biopro.distribution.order.domain.service.PickListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class PickListUseCase implements PickListService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private static final String ORDER_STATUS_OPEN = "OPEN";
    private final PickListMapper pickListMapper;
    private final PickListCommandMapper pickListCommandMapper;

    @Override
    @Transactional
    public Mono<PickList> generatePickList(Long orderId) {
        return orderService.findOneById(orderId)
            .map(pickListMapper::mapToDomain)
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(pickList ->
                Flux.from(inventoryService.getAvailableInventories(pickListCommandMapper.mapToDomain(pickList)))
                    .flatMap(availableInventory -> {
                             var item = pickList.getPickListItems().stream()
                                 .filter(x -> x.getBloodType().equals(availableInventory.getAboRh())
                                     && x.getProductFamily().equals(availableInventory.getProductFamily())).findFirst();

                        item.ifPresent(pickListItem -> availableInventory.getShortDateProducts()
                            .forEach(shortDateProduct -> pickListItem.addShortDate(new PickListItemShortDate(shortDateProduct.getUnitNumber()
                                , shortDateProduct.getProductCode(), shortDateProduct.getStorageLocation()))));

                            return Mono.just(availableInventory);
                        }
                    ).blockLast()
            ).doOnSuccess(this::publishPickListCreatedEvent)
            .onErrorResume(error -> {
                    log.error("Not able to generate pick list {}",error.getMessage());
                    return Mono.error(new RuntimeException("Not Able to Generate Picklist"));
               }
            );

    }

    private void publishPickListCreatedEvent(PickList pickList) {
        log.debug("Publishing PickListCreatedEvent {} , ID {}", pickList, pickList.getOrderNumber());
        if(ORDER_STATUS_OPEN.equals(pickList.getOrderStatus())){
            applicationEventPublisher.publishEvent(new PickListCreatedEvent(pickList));
        }
    }
}
