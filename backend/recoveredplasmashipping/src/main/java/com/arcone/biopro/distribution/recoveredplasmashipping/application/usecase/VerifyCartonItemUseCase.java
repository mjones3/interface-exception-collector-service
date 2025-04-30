package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.VerifyItemCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductCriteriaValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception.ProductValidationException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.VerifyItemCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.VerifyCartonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyCartonItemUseCase implements VerifyCartonService {

    private final InventoryService inventoryService;
    private final CartonItemRepository cartonItemRepository;
    private final RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;
    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final CartonRepository cartonRepository;
    private final CartonOutputMapper cartonOutputMapper;
    private static final String CARTON_DETAILS_PAGE = "/recovered-plasma/%s/carton-details?step=0&reset=true&resetMessage=Products removed due to failure, repeat process";
    private static final String SYSTEM_ERROR_TYPE = "SYSTEM";

    @Override
    @Transactional
    public Mono<UseCaseOutput<CartonOutput>> verifyCartonItem(VerifyItemCommandInput verifyItemCommandInput) {
        return cartonRepository.findOneById(verifyItemCommandInput.cartonId())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", verifyItemCommandInput.cartonId()))))
            .flatMap(carton -> Mono.fromSupplier( () -> carton.verifyItem(new VerifyItemCommand(verifyItemCommandInput.cartonId(), verifyItemCommandInput.unitNumber()
                    , verifyItemCommandInput.productCode(), verifyItemCommandInput.employeeId(), verifyItemCommandInput.locationCode())
                , inventoryService, cartonItemRepository, recoveredPlasmaShipmentCriteriaRepository, recoveredPlasmaShippingRepository)))
            .flatMap(cartonItemRepository::save)
            .flatMap(cartonItemSaved -> {
                return cartonRepository.findOneById(cartonItemSaved.getCartonId())
                    .map(carton -> {
                        return new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                            .builder()
                            .useCaseMessage(UseCaseMessage
                                .builder()
                                .code(UseCaseMessageType.VERIFY_CARTON_ITEM_SUCCESS.getCode())
                                .message(UseCaseMessageType.VERIFY_CARTON_ITEM_SUCCESS.getMessage())
                                .type(UseCaseMessageType.VERIFY_CARTON_ITEM_SUCCESS.getType())
                                .build())
                            .build())
                            , cartonOutputMapper.toOutput(carton)
                            , null);
                    });
            } )
            .doOnError(error -> {
                log.warn("resetting verification {}", error.getMessage());
                this.resetVerification(error,verifyItemCommandInput.cartonId());
            })
            .publishOn(Schedulers.boundedElastic())
            .onErrorResume(error -> {
                log.error("Not able to verify carton Item {}", error.getMessage());
                return buildVerifyErrorResponse(error , verifyItemCommandInput.cartonId());
            });
    }

    private void resetVerification(Throwable error , Long cartonId){
        if(!(error instanceof ProductValidationException productValidationException) || !SYSTEM_ERROR_TYPE.equals(productValidationException.getErrorType())) {
            cartonItemRepository.deleteAllByCartonId(cartonId).subscribe();
        }
    }

    private Mono<UseCaseOutput<CartonOutput>> buildVerifyErrorResponse(Throwable error , Long cartonId) {

        return cartonRepository.findOneById(cartonId).flatMap(carton -> {
            if(error instanceof ProductValidationException productValidationException) {
                var notification = Optional.ofNullable(productValidationException.getInventoryValidation()).map(InventoryValidation::getFistNotification).orElse(null);
                var linksNext = SYSTEM_ERROR_TYPE.equals(productValidationException.getErrorType()) ? null : Map.of("next", String.format(CARTON_DETAILS_PAGE, carton.getId()));
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        Optional.ofNullable(notification).map(firstNotification ->
                            UseCaseMessage
                                .builder()
                                .code(11)
                                .message(firstNotification.getErrorMessage())
                                .type(UseCaseNotificationType.valueOf(firstNotification.getErrorType()))
                                .action(firstNotification.getAction())
                                .reason(firstNotification.getReason())
                                .details(firstNotification.getDetails())
                                .name(firstNotification.getErrorName())
                                .build()
                        ).orElse( UseCaseMessage
                            .builder()
                            .code(11)
                            .message(productValidationException.getMessage())
                            .type(UseCaseNotificationType.valueOf(productValidationException.getErrorType()))
                            .build())
                    )
                    .build()), cartonOutputMapper.toOutput(carton,productValidationException.getInventoryValidation()) ,linksNext));
            }else if(error instanceof ProductCriteriaValidationException productCriteriaValidationException){
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .code(12)
                            .message(productCriteriaValidationException.getMessage())
                            .type(UseCaseNotificationType.valueOf(productCriteriaValidationException.getErrorType()))
                            .name(productCriteriaValidationException.getErrorName())
                            .build()
                    )
                    .build()), cartonOutputMapper.toOutput(carton), Map.of("next", String.format(CARTON_DETAILS_PAGE, carton.getId()))));

            }else{
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .code(13)
                            .message(error.getMessage())
                            .type(UseCaseNotificationType.SYSTEM)
                            .build())
                    .build()), cartonOutputMapper.toOutput(carton), null));
            }
        }).switchIfEmpty(Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
            .builder()
            .useCaseMessage(
                UseCaseMessage
                    .builder()
                    .message(UseCaseMessageType.CARTON_VERIFICATION_ERROR.getMessage())
                    .code(UseCaseMessageType.CARTON_VERIFICATION_ERROR.getCode())
                    .type(UseCaseMessageType.CARTON_VERIFICATION_ERROR.getType())
                    .build())
            .build()), null, null)));
    }
}
