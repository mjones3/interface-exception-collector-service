package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PrintUnacceptableUnitReportCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UnacceptableUnitReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.PrintUnacceptableUnitReportCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.UnacceptableUnitReportRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.UnacceptableUnitReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrintUnacceptableUnitsReportUseCase implements UnacceptableUnitReportService {

    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final LocationRepository locationRepository;
    private final SystemProcessPropertyRepository systemProcessPropertyRepository;
    private final UnacceptableUnitReportRepository unacceptableUnitReportRepository;
    private final RecoveredPlasmaShipmentOutputMapper recoveredPlasmaShipmentOutputMapper;

    @Override
    public Mono<UseCaseOutput<UnacceptableUnitReportOutput>> printUnacceptableUnitReport(PrintUnacceptableUnitReportCommandInput printUnacceptableUnitReportCommandInput) {
        return recoveredPlasmaShippingRepository.findOneById(printUnacceptableUnitReportCommandInput.shipmentId())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", printUnacceptableUnitReportCommandInput.shipmentId()))))
            .flatMap(recoveredPlasmaShipment -> Mono.fromSupplier( () -> recoveredPlasmaShipment.printUnacceptableUnitReport(new PrintUnacceptableUnitReportCommand(printUnacceptableUnitReportCommandInput.shipmentId(),
                printUnacceptableUnitReportCommandInput.employeeId() , printUnacceptableUnitReportCommandInput.locationCode()),unacceptableUnitReportRepository, locationRepository, systemProcessPropertyRepository)))
            .flatMap(unacceptableUnitReport -> {
                    return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                        .builder()
                        .useCaseMessage(
                            UseCaseMessage
                                .builder()
                                .message(UseCaseMessageType.UNACCEPTABLE_UNITS_REPORT_PRINT_SUCCESS.getMessage())
                                .code(UseCaseMessageType.UNACCEPTABLE_UNITS_REPORT_PRINT_SUCCESS.getCode())
                                .type(UseCaseMessageType.UNACCEPTABLE_UNITS_REPORT_PRINT_SUCCESS.getType())
                                .build())
                        .build())
                        , recoveredPlasmaShipmentOutputMapper.toUnacceptableUnitReportOutput(unacceptableUnitReport)
                        , null));
            }).onErrorResume(error -> {
                log.error("Error printing unacceptable units report {}", error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.UNACCEPTABLE_UNITS_REPORT_PRINT_ERROR.getMessage())
                            .code(UseCaseMessageType.UNACCEPTABLE_UNITS_REPORT_PRINT_ERROR.getCode())
                            .type(UseCaseMessageType.UNACCEPTABLE_UNITS_REPORT_PRINT_ERROR.getType())
                            .build())
                    .build()), null, null));
            });
    }
}
