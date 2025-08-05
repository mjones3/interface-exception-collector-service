package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.dto.EnterShippingInformationCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ShippingInformationOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.model.EnterShippingInformationCommand;
import com.arcone.biopro.distribution.receiving.domain.model.ShippingInformation;
import com.arcone.biopro.distribution.receiving.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import com.arcone.biopro.distribution.receiving.domain.service.ShippingInformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterShippingInformationUseCase implements ShippingInformationService {

    private final ProductConsequenceRepository productConsequenceRepository;
    private final LookupRepository lookupRepository;
    private final ShippingInformationOutputMapper shippingInformationOutputMapper;
    private final LocationRepository locationRepository;

    @Override
    public Mono<UseCaseOutput<ShippingInformationOutput>> enterShippingInformation(EnterShippingInformationCommandInput enterShippingInformationCommandInput) {
        return Mono.fromCallable(() -> ShippingInformation.fromNewImportBatch(new EnterShippingInformationCommand(enterShippingInformationCommandInput.productCategory()
                , enterShippingInformationCommandInput.employeeId() , enterShippingInformationCommandInput.locationCode()),lookupRepository,productConsequenceRepository,locationRepository))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(shippingInformation -> Mono.just(new UseCaseOutput<>(Collections.emptyList(), shippingInformationOutputMapper.mapToOutput(shippingInformation), null)))
            .onErrorResume(error -> {
                log.error("Not able create shipping information {}",error.getMessage());
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
            });
    }
}
