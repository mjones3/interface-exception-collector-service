package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonPackingSlipOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.GenerateCartonPackingSlipCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CartonPackingSlipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateCartonPackingSlipUseCase implements CartonPackingSlipService {

    private final RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;
    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final CartonRepository cartonRepository;
    private final LocationRepository locationRepository;
    private final SystemProcessPropertyRepository systemProcessPropertyRepository;
    private final CartonOutputMapper cartonOutputMapper;

    @Override
    public Mono<UseCaseOutput<CartonPackingSlipOutput>> generateCartonPackingSlip(GenerateCartonPackingSlipCommandInput generateCartonPackingSlipCommandInput) {
        return cartonRepository.findOneById(generateCartonPackingSlipCommandInput.cartonId())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", generateCartonPackingSlipCommandInput.cartonId()))))
            .flatMap(carton -> Mono.fromSupplier( () -> carton.generatePackingSlip(locationRepository,systemProcessPropertyRepository,recoveredPlasmaShippingRepository,recoveredPlasmaShipmentCriteriaRepository)))
            .flatMap(cartonPackingSlip -> {
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_SUCCESS.getMessage())
                            .code(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_SUCCESS.getCode())
                            .type(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_SUCCESS.getType())
                            .build())
                    .build())
                    , cartonOutputMapper.toOutPut(cartonPackingSlip)
                    , null));
            })
            .onErrorResume(error -> {
                log.error("Error generating carton packing slip {}", error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_ERROR.getMessage())
                            .code(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_ERROR.getCode())
                            .type(UseCaseMessageType.CARTON_PACKING_SLIP_GENERATED_ERROR.getType())
                            .build())
                    .build()), null, null));
            });

    }
}
