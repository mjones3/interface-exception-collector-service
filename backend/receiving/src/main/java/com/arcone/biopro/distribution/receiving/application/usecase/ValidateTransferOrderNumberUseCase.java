package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTransferOrderNumberCommandInput;
import com.arcone.biopro.distribution.receiving.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.receiving.application.mapper.ShippingInformationOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.model.EnterShippingInformationCommand;
import com.arcone.biopro.distribution.receiving.domain.model.ShippingInformation;
import com.arcone.biopro.distribution.receiving.domain.repository.InternalTransferRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import com.arcone.biopro.distribution.receiving.domain.service.ValidateTransferOrderNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidateTransferOrderNumberUseCase implements ValidateTransferOrderNumberService {

    private final ProductConsequenceRepository productConsequenceRepository;
    private final LookupRepository lookupRepository;
    private final ShippingInformationOutputMapper shippingInformationOutputMapper;
    private final LocationRepository locationRepository;
    private final InternalTransferRepository internalTransferRepository;

    @Override
    public Mono<UseCaseOutput<ShippingInformationOutput>> validateTransferOrderNumber(ValidateTransferOrderNumberCommandInput validateTransferOrderNumberCommandInput) {

        return internalTransferRepository.findOneByOrderNumber(validateTransferOrderNumberCommandInput.orderNumber())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(new DomainNotFoundForKeyException(validateTransferOrderNumberCommandInput.orderNumber().toString())))
            .flatMap(internalTransfer -> Mono.fromCallable(() -> ShippingInformation.fromNewTransferReceipt(new EnterShippingInformationCommand(internalTransfer.getTemperatureCategory()
                , validateTransferOrderNumberCommandInput.employeeId() , validateTransferOrderNumberCommandInput.locationCode()),lookupRepository,productConsequenceRepository,locationRepository,internalTransfer)))
            .flatMap(shippingInformation -> Mono.just(new UseCaseOutput<>(Collections.emptyList(), shippingInformationOutputMapper.mapToOutput(shippingInformation), null)))
            .onErrorResume(error -> {
                log.error("Not able create shipping information {}",error.getMessage());
                if(error instanceof DomainNotFoundForKeyException){
                    return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                        .builder()
                        .useCaseMessage(
                            UseCaseMessage
                                .builder()
                                .message(UseCaseMessageType.INTERNAL_TRANSFER_NOT_FOUND_ERROR.getMessage())
                                .code(UseCaseMessageType.INTERNAL_TRANSFER_NOT_FOUND_ERROR.getCode())
                                .type(UseCaseMessageType.INTERNAL_TRANSFER_NOT_FOUND_ERROR.getType())
                                .build())
                        .build()), null, null));
                }else{
                    return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                        .builder()
                        .useCaseMessage(
                            UseCaseMessage
                                .builder()
                                .message(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_ERROR.getMessage())
                                .code(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_ERROR.getCode())
                                .type(UseCaseMessageType.ENTER_SHIPPING_INFORMATION_ERROR.getType())
                                .build())
                        .build()), null, null));
                }
            });

    }
}
