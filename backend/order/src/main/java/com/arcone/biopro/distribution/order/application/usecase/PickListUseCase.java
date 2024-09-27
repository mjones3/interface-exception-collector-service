package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.order.application.dto.UseCaseNotificationDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.order.application.exception.ServiceNotAvailableException;
import com.arcone.biopro.distribution.order.application.mapper.PickListCommandMapper;
import com.arcone.biopro.distribution.order.application.mapper.PickListMapper;
import com.arcone.biopro.distribution.order.domain.event.PickListCreatedEvent;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.domain.model.PickListItemShortDate;
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

import java.util.List;

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
    public Mono<UseCaseResponseDTO<PickList>> generatePickList(final Long orderId , final boolean skipInventoryUnavailable) {
        return orderService.findOneById(orderId)
            .map(pickListMapper::mapToUseCaseResponse)
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(useCaseResponse ->
               Flux.from(inventoryService.getAvailableInventories(pickListCommandMapper.mapToDomain(useCaseResponse.data())).onErrorResume(error -> {
                            if(skipInventoryUnavailable) {
                                log.debug("Skipping inventory unavailable.");
                                return Mono.empty();
                            }else{
                                log.error("Not able to fetch inventory Data {}", error.getMessage());
                                return Mono.error(new ServiceNotAvailableException("Inventory Service Not Available"));
                            }
                        })
                    )
                    .flatMap(availableInventory -> {
                            var item = useCaseResponse.data().getPickListItems().stream()
                                .filter(x -> x.getBloodType().equals(availableInventory.getAboRh())
                                    && x.getProductFamily().equals(availableInventory.getProductFamily())).findFirst();

                            item.ifPresent(pickListItem -> availableInventory.getShortDateProducts()
                                .forEach(shortDateProduct -> pickListItem.addShortDate(new PickListItemShortDate(shortDateProduct.getUnitNumber()
                                    , shortDateProduct.getProductCode() , shortDateProduct.getAboRh(), shortDateProduct.getStorageLocation()))));

                            return Mono.just(availableInventory);
                        }
                    ).blockLast()

            )
            .doOnSuccess(this::publishPickListCreatedEvent)
            .onErrorResume(error -> {
                if(error instanceof ServiceNotAvailableException) {
                    return Mono.just(buildErrorResponse());
                }else{
                    log.error("Not able to generate pick list {}",error.getMessage());
                    return Mono.error(new RuntimeException("Not Able to Generate Picklist"));
                }
               }
            );

    }

    private void publishPickListCreatedEvent(UseCaseResponseDTO<PickList> useCaseResponseDTO) {
        log.debug("Publishing PickListCreatedEvent {} , ID {}", useCaseResponseDTO.data(), useCaseResponseDTO.data().getOrderNumber());
        if(ORDER_STATUS_OPEN.equals(useCaseResponseDTO.data().getOrderStatus())){
            applicationEventPublisher.publishEvent(new PickListCreatedEvent(useCaseResponseDTO.data()));
        }
    }

    private UseCaseResponseDTO<PickList> buildErrorResponse(){
        return new UseCaseResponseDTO<>(List.of(UseCaseNotificationDTO
            .builder()
            .useCaseMessageType(UseCaseMessageType.INVENTORY_SERVICE_IS_DOWN)
            .build()), null);
    }
}
