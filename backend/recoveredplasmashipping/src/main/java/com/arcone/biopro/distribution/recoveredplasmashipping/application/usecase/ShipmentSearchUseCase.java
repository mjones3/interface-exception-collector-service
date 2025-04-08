package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PageOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentQueryCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.PageOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentQueryCommandInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentReportRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RecoveredPlasmaShipmentReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentSearchUseCase implements RecoveredPlasmaShipmentReportService {

    private final RecoveredPlasmaShipmentQueryCommandInputMapper recoveredPlasmaShipmentQueryCommandInputMapper;
    private final PageOutputMapper pageOutputMapper;
    private final RecoveredPlasmaShipmentReportRepository recoveredPlasmaShipmentReportRepository;

    @Override
    public Mono<UseCaseOutput<PageOutput<RecoveredPlasmaShipmentReportOutput>>> search(RecoveredPlasmaShipmentQueryCommandInput recoveredPlasmaShipmentQueryCommandInput) {
        return recoveredPlasmaShipmentReportRepository.search(recoveredPlasmaShipmentQueryCommandInputMapper.toModel(recoveredPlasmaShipmentQueryCommandInput))
            .switchIfEmpty(Mono.error(NoResultsFoundException::new))
            .flatMap(recoveredPlasmaShipmentReportPage -> {
                if (recoveredPlasmaShipmentReportPage.getContent() == null || recoveredPlasmaShipmentReportPage.getContent().isEmpty()) {
                    return Mono.error(NoResultsFoundException::new);
                }
                return Mono.just(new UseCaseOutput<>(null
                    , pageOutputMapper.toPageOutput(recoveredPlasmaShipmentReportPage)
                    , null ));
            }).onErrorResume(error -> {
                log.error("Error searching shipments", error);
                return Mono.just(buildErrorResponse(error));
            });
    }

    private UseCaseOutput<PageOutput<RecoveredPlasmaShipmentReportOutput>> buildErrorResponse(Throwable error) {
        return new UseCaseOutput<>(List.of(UseCaseNotificationOutput
            .builder()
            .useCaseMessage(new UseCaseMessage(4, UseCaseNotificationType.CAUTION, error.getMessage()))
            .build()), null, null);

    }
}
