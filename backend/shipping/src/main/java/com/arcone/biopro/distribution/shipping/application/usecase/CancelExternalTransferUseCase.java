package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.CancelExternalTransferRequest;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.shipping.application.exception.DomainException;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import com.arcone.biopro.distribution.shipping.domain.service.CancelExternalTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CancelExternalTransferUseCase extends AbstractUseCase implements CancelExternalTransferService {

    private final ExternalTransferRepository externalTransferRepository;
    private final static String EXTERNAL_TRANSFER_URL  = "/external-transfer";

    @Override
    public Mono<RuleResponseDTO> cancelExternalTransfer(CancelExternalTransferRequest cancelExternalTransferRequest) {
        return externalTransferRepository.findOneById(cancelExternalTransferRequest.externalTransferId())
            .switchIfEmpty(Mono.error(new DomainException(UseCaseMessageType.EXTERNAL_TRANSFER_NOT_FOUND)))
            .flatMap(externalTransfer -> Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.OK)
                .notifications(List.of(NotificationDTO.builder()
                    .name("EXTERNAL_TRANSFER_CANCEL_CONFIRMATION")
                    .statusCode(HttpStatus.OK.value())
                    .code(HttpStatus.OK.value())
                    .message(ShipmentServiceMessages.EXTERNAL_TRANSFER_CANCEL_CONFIRMATION)
                    .notificationType(NotificationType.CONFIRMATION.name())
                    .build()))
                .build()))
            .onErrorResume(error -> {
                log.error("Not able to cancel external transfer process {} error {}", cancelExternalTransferRequest,error.getMessage());
                return buildErrorResponse(error);
            });
    }

    @Override
    @Transactional
    public Mono<RuleResponseDTO> confirmCancelExternalTransfer(CancelExternalTransferRequest cancelExternalTransferRequest) {
        return externalTransferRepository.findOneById(cancelExternalTransferRequest.externalTransferId())
            .switchIfEmpty(Mono.error(new DomainException(UseCaseMessageType.EXTERNAL_TRANSFER_NOT_FOUND)))
            .flatMap(externalTransfer -> externalTransferRepository.deleteOneById(externalTransfer.getId()))
            .then(Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.OK)
                .notifications(List.of(NotificationDTO.builder()
                        .message(ShipmentServiceMessages.EXTERNAL_TRANSFER_CANCELLED_SUCCESS)
                        .statusCode(HttpStatus.OK.value())
                        .notificationType(NotificationType.SUCCESS.name())
                        .build()))
                    ._links(Map.of("next",EXTERNAL_TRANSFER_URL))
                .build()))
            .onErrorResume(error -> {
                log.error("Not able to confirm cancel external transfer process {} error {}", cancelExternalTransferRequest,error.getMessage());
                return buildErrorResponse(error);
            });
    }
}
