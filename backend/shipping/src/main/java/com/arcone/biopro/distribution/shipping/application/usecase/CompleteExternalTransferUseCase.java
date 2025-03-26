package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.CompleteExternalTransferCommandDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.shipping.application.exception.DomainException;
import com.arcone.biopro.distribution.shipping.application.mapper.ExternalTransferDomainMapper;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.event.ExternalTransferCompletedEvent;
import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import com.arcone.biopro.distribution.shipping.domain.service.CompleteExternalTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompleteExternalTransferUseCase extends AbstractUseCase implements CompleteExternalTransferService {

    private final ExternalTransferRepository externalTransferRepository;
    private final ExternalTransferDomainMapper externalTransferDomainMapper;
    private final static String EXTERNAL_TRANSFER_URL  = "/external-transfer";
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    public Mono<RuleResponseDTO> completeExternalTransfer(CompleteExternalTransferCommandDTO completeExternalTransferCommandDTO) {
        return externalTransferRepository.findOneById(completeExternalTransferCommandDTO.externalTransferId())
            .switchIfEmpty(Mono.error(new DomainException(UseCaseMessageType.EXTERNAL_TRANSFER_NOT_FOUND)))
            .publishOn(Schedulers.boundedElastic())
            .flatMap(externalTransfer -> {
                externalTransfer.complete(completeExternalTransferCommandDTO.hospitalTransferId(), completeExternalTransferCommandDTO.employeeId());
                return externalTransferRepository.update(externalTransfer);
            })
            .flatMap(externalTransfer -> {
                applicationEventPublisher.publishEvent(new ExternalTransferCompletedEvent(externalTransfer));
                return Mono.just(RuleResponseDTO.builder()
                    .ruleCode(HttpStatus.OK)
                    .results(Map.of("results", List.of(externalTransferDomainMapper.toDTO(externalTransfer))))
                    .notifications(List.of(NotificationDTO.builder()
                        .message(ShipmentServiceMessages.EXTERNAL_TRANSFER_COMPLETED_SUCCESS)
                        .statusCode(HttpStatus.OK.value())
                        .notificationType(NotificationType.SUCCESS.name())
                        .build()))
                    ._links(Map.of("next",EXTERNAL_TRANSFER_URL))
                    .build());
            })
            .onErrorResume(error -> {
                log.error("Not able to complete external Transfer {} error {}", completeExternalTransferCommandDTO,error.getMessage());
                return buildErrorResponse(error);
            });
    }

}
