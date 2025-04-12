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
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductCriteriaValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
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
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;

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
    @Transactional
    public Mono<UseCaseOutput<CartonItemOutput>> packItem(PackCartonItemCommandInput packCartonItemCommandInput) {
        return cartonRepository.findOneById(packCartonItemCommandInput.cartonId())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", packCartonItemCommandInput.cartonId()))))
            .flatMap(carton -> Mono.fromSupplier( () -> carton.packItem(new PackItemCommand(packCartonItemCommandInput.cartonId(), packCartonItemCommandInput.unitNumber(), packCartonItemCommandInput.productCode(), packCartonItemCommandInput.employeeId(), packCartonItemCommandInput.locationCode())
                , inventoryService, cartonItemRepository, recoveredPlasmaShipmentCriteriaRepository, recoveredPlasmaShippingRepository)))
            .flatMap(cartonItemRepository::save)
            .flatMap(cartonItemSaved -> {
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(UseCaseMessage
                        .builder()
                        .code(UseCaseMessageType.CARTON_ITEM_PACKED_SUCCESS.getCode())
                        .message(UseCaseMessageType.CARTON_ITEM_PACKED_SUCCESS.getMessage())
                        .type(UseCaseMessageType.CARTON_ITEM_PACKED_SUCCESS.getType())
                        .build())
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

        if(error instanceof ProductValidationException productValidationException) {
            var notification = Optional.ofNullable(productValidationException.getInventoryValidation()).map(InventoryValidation::getFistNotification).orElse(null);
            return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                .builder()
                .useCaseMessage(
                    Optional.ofNullable(notification).map(firstNotification ->
                        UseCaseMessage
                            .builder()
                            .code(6)
                            .message(firstNotification.getErrorMessage())
                            .type(UseCaseNotificationType.valueOf(firstNotification.getErrorType()))
                            .action(firstNotification.getAction())
                            .reason(firstNotification.getReason())
                            .details(firstNotification.getDetails())
                            .build()
                    ).orElse( UseCaseMessage
                        .builder()
                        .code(6)
                            .message(productValidationException.getMessage())
                            .type(UseCaseNotificationType.valueOf(productValidationException.getErrorType()))
                        .build())
                    )
                .build()), cartonItemOutputMapper.toOutput(productValidationException.getInventoryValidation()), null));
        }else if(error instanceof ProductCriteriaValidationException productCriteriaValidationException){
            return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                .builder()
                .useCaseMessage(
                    UseCaseMessage
                        .builder()
                        .code(7)
                        .message(productCriteriaValidationException.getMessage())
                        .type(UseCaseNotificationType.valueOf(productCriteriaValidationException.getErrorType()))
                        .build()
                    )
                .build()), null, null));

        }else{
            return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                .builder()
                .useCaseMessage(

                    UseCaseMessage
                        .builder()
                        .code(8)
                        .message(error.getMessage())
                        .type(UseCaseNotificationType.WARN)
                        .build())
                .build()), null, null));
        }


    }
}
