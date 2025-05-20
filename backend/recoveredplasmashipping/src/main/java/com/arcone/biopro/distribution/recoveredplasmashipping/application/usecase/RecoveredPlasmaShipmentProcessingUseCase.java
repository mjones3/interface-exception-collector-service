package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentClosedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentProcessingEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ValidateInventoryCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.UnacceptableUnitReportRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveredPlasmaShipmentProcessingUseCase {

    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final InventoryService inventoryService;
    private final CartonItemRepository cartonItemRepository;
    private final UnacceptableUnitReportRepository unacceptableUnitReportRepository;
    private final CartonRepository cartonRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private static  final String  INVENTORY_PACKED_ERROR_TYPE = "INVENTORY_IS_PACKED";

    @EventListener
    @Transactional
    public Mono<RecoveredPlasmaShipment> onRecoveredPlasmaShipmentProcessing(RecoveredPlasmaShipmentProcessingEvent recoveredPlasmaShipmentProcessingEvent){
       log.debug("Processing RecoveredPlasmaShipmentProcessing Event {}",recoveredPlasmaShipmentProcessingEvent);
       return recoveredPlasmaShippingRepository.findOneById(recoveredPlasmaShipmentProcessingEvent.getPayload().getId())
           .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", recoveredPlasmaShipmentProcessingEvent.getPayload().getId()))))
           .publishOn(Schedulers.boundedElastic())
           .doOnNext(recoveredPlasmaShipment -> unacceptableUnitReportRepository.deleteAllByShipmentId(recoveredPlasmaShipment.getId()).subscribe())
           .flatMap(recoveredPlasmaShipment -> {
               return processCartonItems(recoveredPlasmaShipment)
                   .collectList()
                   .flatMap(unacceptableUnitReportItemList -> recoveredPlasmaShippingRepository.update(recoveredPlasmaShipment.completeProcessing(unacceptableUnitReportItemList)));
           }).doOnSuccess(recoveredPlasmaShipment -> {
               if("CLOSED".equals(recoveredPlasmaShipment.getStatus())){
                log.debug("Publishing Recovered Plasma Shipment Closed event {}",recoveredPlasmaShipment);
                   applicationEventPublisher.publishEvent(new RecoveredPlasmaShipmentClosedEvent(recoveredPlasmaShipment));
               }
           })
           .onErrorResume(error -> {
               log.error("Not able to process RecoveredPlasmaShipmentProcessing Event {}", error.getMessage());
               if(error instanceof DomainNotFoundForKeyException){
                   return Mono.empty();
               }
               return recoveredPlasmaShippingRepository.findOneById(recoveredPlasmaShipmentProcessingEvent.getPayload().getId())
                   .flatMap(recoveredPlasmaShipment -> recoveredPlasmaShippingRepository.update(recoveredPlasmaShipment.markAsProcessingError()));
           });
    }


    private Flux<UnacceptableUnitReportItem> processCartonItems(RecoveredPlasmaShipment recoveredPlasmaShipment){
        return inventoryService.validateInventoryBatch(this.getValidateCommand(recoveredPlasmaShipment))
            .flatMap(inventoryValidation -> this.processInventoryResponse(inventoryValidation,recoveredPlasmaShipment));
    }

    private Flux<ValidateInventoryCommand> getValidateCommand(RecoveredPlasmaShipment recoveredPlasmaShipment){
        return cartonItemRepository.findAllByShipmentId(recoveredPlasmaShipment.getId())
            .map(cartonItem -> {
                return new ValidateInventoryCommand(cartonItem.getUnitNumber()
                    , cartonItem.getProductCode(), recoveredPlasmaShipment.getLocationCode());
            }
        );
    }

    private Mono<UnacceptableUnitReportItem> processInventoryResponse(InventoryValidation inventoryValidation , RecoveredPlasmaShipment recoveredPlasmaShipment){

        var notification = inventoryValidation.getFirstNotification();
        if (notification != null && !INVENTORY_PACKED_ERROR_TYPE.equals(notification.getErrorName()) ) {
            var inventory = inventoryValidation.getInventory();
            return cartonItemRepository.findOneByShipmentIdAndProduct(recoveredPlasmaShipment.getId(), inventory.getUnitNumber(),inventory.getProductCode())
                .flatMap(cartonItem -> cartonRepository.findOneById(cartonItem.getCartonId())
                    .flatMap(carton -> cartonRepository.update(carton.markAsRepack()))
                    .flatMap(cartonRepacked -> unacceptableUnitReportRepository.save(new UnacceptableUnitReportItem(recoveredPlasmaShipment.getId()
                        , cartonRepacked.getCartonNumber(), cartonRepacked.getCartonSequence()
                        , cartonItem.getUnitNumber(), cartonItem.getProductCode(), notification.getErrorMessage(), ZonedDateTime.now()))));
        } else {
            return Mono.empty();
        }
    }
}
