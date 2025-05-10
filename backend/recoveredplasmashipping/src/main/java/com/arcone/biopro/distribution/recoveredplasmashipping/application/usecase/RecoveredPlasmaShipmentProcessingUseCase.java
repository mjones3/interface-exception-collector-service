package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentProcessingEvent;
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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.ZonedDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveredPlasmaShipmentProcessingUseCase {

    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final InventoryService inventoryService;
    private final CartonItemRepository cartonItemRepository;
    private final UnacceptableUnitReportRepository unacceptableUnitReportRepository;
    private final CartonRepository cartonRepository;

    @EventListener
    @Transactional
    public Mono<RecoveredPlasmaShipment> onRecoveredPlasmaShipmentProcessing(RecoveredPlasmaShipmentProcessingEvent recoveredPlasmaShipmentProcessingEvent){
       log.debug("Processing RecoveredPlasmaShipmentProcessing Event {}",recoveredPlasmaShipmentProcessingEvent);

        var reportItemArrayList = new ArrayList<UnacceptableUnitReportItem>();

       return recoveredPlasmaShippingRepository.findOneById(recoveredPlasmaShipmentProcessingEvent.getPayload().getId())
           .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", recoveredPlasmaShipmentProcessingEvent.getPayload().getId()))))
           .publishOn(Schedulers.boundedElastic())
           .doOnNext(recoveredPlasmaShipment -> unacceptableUnitReportRepository.deleteAllByShipmentId(recoveredPlasmaShipment.getId()).subscribe())
           .flatMap(recoveredPlasmaShipment -> Flux.from(cartonItemRepository.findAllByShipmentId(recoveredPlasmaShipment.getId()))
               .flatMap(cartonItem -> {
                   return inventoryService.validateInventoryBatch(Flux.just(new ValidateInventoryCommand(cartonItem.getUnitNumber(),cartonItem.getProductCode(), recoveredPlasmaShipment.getLocationCode())))
                       .flatMap(inventoryValidation -> {
                           if(inventoryValidation.getFistNotification() != null){
                               var notification = inventoryValidation.getFistNotification();
                               return cartonRepository.findOneById(cartonItem.getCartonId())
                                   .flatMap(carton -> cartonRepository.update(carton.markAsRepack()))
                                   .flatMap(cartonRepacked -> unacceptableUnitReportRepository.save(new UnacceptableUnitReportItem(recoveredPlasmaShipment.getId()
                                       , cartonRepacked.getCartonNumber() , cartonRepacked.getCartonSequence()
                                       , cartonItem.getUnitNumber() , cartonItem.getProductCode(), notification.getErrorMessage(), ZonedDateTime.now())))
                                   .flatMap(reportItem -> {
                                       reportItemArrayList.add(reportItem);
                                       return Mono.just(reportItem);
                                   });
                           }else{
                               return Mono.empty();
                           }
                       })
                       .then();
               })
               .collectList()
               .flatMap(list -> {
                   log.debug("Total Flagged products {}",reportItemArrayList.size());
                   return recoveredPlasmaShippingRepository.update(recoveredPlasmaShipment.completeProcessing(reportItemArrayList));
               })
           ).onErrorResume(error -> {
               log.error("Not able to process RecoveredPlasmaShipmentProcessing Event {}", error.getMessage());
               if(error instanceof DomainNotFoundForKeyException){
                   return Mono.empty();
               }
               return recoveredPlasmaShippingRepository.findOneById(recoveredPlasmaShipmentProcessingEvent.getPayload().getId())
                   .flatMap(recoveredPlasmaShipment -> recoveredPlasmaShippingRepository.update(recoveredPlasmaShipment.markAsProcessingError()));
           });
    }
}
