package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonItemOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PackCartonItemCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonItemOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.PackItemCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.PackCartonItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackCartonItemUseCase implements PackCartonItemService {

    private final InventoryService inventoryService;
    private final CartonItemRepository cartonItemRepository;
    private final RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;
    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final CartonRepository cartonRepository;
    private final CartonItemOutputMapper cartonItemOutputMapper;

    @Override
    public Mono<UseCaseOutput<CartonItemOutput>> packItem(PackCartonItemCommandInput packCartonItemCommandInput) {
        return cartonRepository.findOneById(packCartonItemCommandInput.cartonId())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", packCartonItemCommandInput.cartonId()))))
            .flatMap(carton -> Mono.fromSupplier( () -> CartonItem.createNewCartonItem(new PackItemCommand(packCartonItemCommandInput.cartonId(), packCartonItemCommandInput.unitNumber(), packCartonItemCommandInput.productCode(), packCartonItemCommandInput.employeeId(), packCartonItemCommandInput.locationCode()), carton
                , inventoryService, cartonItemRepository, recoveredPlasmaShippingRepository, recoveredPlasmaShipmentCriteriaRepository)))
            .flatMap(cartonItemRepository::save)
            .flatMap(cartonItemSaved -> {
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(new UseCaseMessage(UseCaseMessageType.CARTON_CREATED_SUCCESS))
                    .build())
                    , cartonItemOutputMapper.toOutput(cartonItemSaved)
                    , null));
            } )
            .onErrorResume(error -> {
                log.error("Not able to create carton Item {}", error.getMessage());
                return buildPackErrorResponse(error);
            });

    }

    private Mono<UseCaseOutput<CartonItemOutput>> buildPackErrorResponse(Throwable error) {

        if(error instanceof ProductValidationException productValidationException){
            return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                .builder()
                .useCaseMessage(new UseCaseMessage(6, UseCaseNotificationType.valueOf(productValidationException.getErrorType()), productValidationException.getMessage()))
                .build()), null, null));
        }else{
            return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                .builder()
                .useCaseMessage(new UseCaseMessage(6, UseCaseNotificationType.WARN, error.getMessage()))
                .build()), null, null));
        }


    }
}
