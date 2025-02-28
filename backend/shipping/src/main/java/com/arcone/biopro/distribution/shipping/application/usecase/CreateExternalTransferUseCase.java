package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ExternalTransferDomainMapper;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.CreateExternalTransferCommand;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import com.arcone.biopro.distribution.shipping.domain.service.CreateExternalTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateExternalTransferUseCase implements CreateExternalTransferService {

    private final ExternalTransferRepository externalTransferRepository;
    private final ExternalTransferDomainMapper externalTransferDomainMapper;


    @Override
    public Mono<RuleResponseDTO> createExternalTransfer(CreateExternalTransferCommand createExternalTransferCommand) {
        return externalTransferDomainMapper.toDomain(createExternalTransferCommand)
            .flatMap(externalTransferRepository::create)
            .flatMap(externalTransfer -> Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.OK)
                .results(Map.of("results", List.of(externalTransferDomainMapper.toDTO(externalTransfer))))
                .notifications(List.of(NotificationDTO.builder()
                    .message(ShipmentServiceMessages.EXTERNAL_TRANSFER_CREATED_SUCCESS)
                    .statusCode(HttpStatus.OK.value())
                    .notificationType(NotificationType.SUCCESS.name())
                    .build()))
                .build()))
            .onErrorResume(error -> {
                log.error("No external transfer created for {} error {}", createExternalTransferCommand,error.getMessage());
                return Mono.just(RuleResponseDTO.builder()
                    .ruleCode(HttpStatus.BAD_REQUEST)
                    .notifications(List.of(NotificationDTO
                        .builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(error.getMessage())
                        .notificationType(NotificationType.WARN.name())
                        .build()))
                    .build());
            });
    }

}
