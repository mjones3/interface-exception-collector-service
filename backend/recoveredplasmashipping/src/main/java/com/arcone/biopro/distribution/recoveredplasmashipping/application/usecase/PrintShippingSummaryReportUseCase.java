package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PrintShippingSummaryReportCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShippingSummaryReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.ShippingSummaryReportOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.PrintShippingSummaryCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.ShippingSummaryReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrintShippingSummaryReportUseCase implements ShippingSummaryReportService {

    private final RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;
    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final CartonRepository cartonRepository;
    private final LocationRepository locationRepository;
    private final SystemProcessPropertyRepository systemProcessPropertyRepository;
    private final ShippingSummaryReportOutputMapper shippingSummaryReportOutputMapper;

    @Override
    public Mono<UseCaseOutput<ShippingSummaryReportOutput>> printShippingSummaryReport(PrintShippingSummaryReportCommandInput printShippingSummaryReportCommandInput) {
        return recoveredPlasmaShippingRepository.findOneById(printShippingSummaryReportCommandInput.shipmentId())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", printShippingSummaryReportCommandInput.shipmentId()))))
            .flatMap(shipment -> Mono.fromSupplier( () -> shipment.printShippingSummaryReport(new PrintShippingSummaryCommand(printShippingSummaryReportCommandInput.shipmentId()
                , printShippingSummaryReportCommandInput.employeeId()
                , printShippingSummaryReportCommandInput.locationCode()),cartonRepository,systemProcessPropertyRepository,recoveredPlasmaShipmentCriteriaRepository,locationRepository)))
            .flatMap(shippingSummaryReport -> {
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_SUCCESS.getMessage())
                            .code(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_SUCCESS.getCode())
                            .type(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_SUCCESS.getType())
                            .build())
                    .build())
                    , shippingSummaryReportOutputMapper.toOutput(shippingSummaryReport)
                    , null));
            })
            .onErrorResume(error -> {
                log.error("Error generating shipping summary report {}", error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_ERROR.getMessage())
                            .code(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_ERROR.getCode())
                            .type(UseCaseMessageType.PRINT_SHIPPING_SUMMARY_REPORT_ERROR.getType())
                            .build())
                    .build()), null, null));
            });
    }
}
