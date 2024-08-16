package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.domain.event.PickListCreatedEvent;
import com.arcone.biopro.distribution.order.domain.model.GeneratePickListCommand;
import com.arcone.biopro.distribution.order.domain.model.GeneratePickListProductCriteria;
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

import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class PickListUseCase implements PickListService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public Mono<PickList> generatePickList(Long orderId) {

        return orderService.findOneById(orderId)
            .map(order -> {
                    var pickList = new PickList(order.getOrderNumber().getOrderNumber() , order.getLocationCode()
                        , new PickListCustomer(order.getShippingCustomer().getCode() , order.getShippingCustomer().getName()));
                    if(order.getOrderItems() != null){
                        order.getOrderItems().forEach(orderItem -> pickList.addPickListItem(new PickListItem(orderItem.getProductFamily().getProductFamily()
                            , orderItem.getBloodType().getBloodType() , orderItem.getQuantity() , orderItem.getComments() )));
                    }
                    return pickList;
                }
                )
            .doOnNext(pickList ->
                Flux.from(inventoryService.getAvailableInventories(new GeneratePickListCommand(pickList.getLocationCode(),mapCriteriaList(pickList))))
                    .flatMap(availableInventory -> {
                             var item = pickList.getPickListItems().stream()
                                 .filter(x -> x.getBloodType().equals(availableInventory.getAboRh())
                                     && x.getProductFamily().equals(availableInventory.getProductFamily())).findFirst();

                        item.ifPresent(pickListItem -> availableInventory.getShortDateProducts()
                            .forEach(shortDateProduct -> pickListItem.addShortDate(new PickListItemShortDate(shortDateProduct.getUnitNumber()
                                , shortDateProduct.getProductCode(), shortDateProduct.getStorageLocation()))));

                            return Mono.just(availableInventory);
                        }
                    )
                    .then(Mono.just(pickList))
            ).doOnSuccess(this::publishPickListCreatedEvent)
            .onErrorResume(error -> Mono.error(new RuntimeException("Not Able to Generate Picklist")));

    }

    private List<GeneratePickListProductCriteria> mapCriteriaList(PickList pickList) {
        return ofNullable(pickList.getPickListItems())
            .filter(pickListItems -> !pickListItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(pickListItem -> new GeneratePickListProductCriteria(pickListItem.getProductFamily(), pickListItem.getBloodType()))
            .toList();
    }


    private void publishPickListCreatedEvent(PickList pickList) {
        log.debug("Publishing PickListCreatedEvent {} , ID {}", pickList, pickList.getOrderNumber());
        applicationEventPublisher.publishEvent(new PickListCreatedEvent(pickList));

    }
}
