package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.AddProductTransferCommandDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.shipping.application.exception.DomainException;
import com.arcone.biopro.distribution.shipping.application.mapper.ExternalTransferDomainMapper;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ProductLocationHistoryRepository;
import com.arcone.biopro.distribution.shipping.domain.service.AddProductTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddProductTransferUseCase extends AbstractUseCase implements AddProductTransferService {

    private final ExternalTransferRepository externalTransferRepository;
    private final ProductLocationHistoryRepository productLocationHistoryRepository;
    private final ExternalTransferDomainMapper externalTransferDomainMapper;

    @Override
    public Mono<RuleResponseDTO> addProductTransfer(AddProductTransferCommandDTO addProductTransferCommandDTO) {
        return externalTransferRepository.findOneById(addProductTransferCommandDTO.externalTransferId())
            .switchIfEmpty(Mono.error(new DomainException(UseCaseMessageType.EXTERNAL_TRANSFER_NOT_FOUND)))
            .publishOn(Schedulers.boundedElastic())
            .flatMap(externalTransfer -> {
                externalTransfer.addItem(null, addProductTransferCommandDTO.unitNumber()
                    , addProductTransferCommandDTO.productCode(), addProductTransferCommandDTO.employeeId(), productLocationHistoryRepository);
                return externalTransferRepository.update(externalTransfer);
            })
            .flatMap(externalTransfer -> {
                return Mono.just(RuleResponseDTO.builder()
                    .ruleCode(HttpStatus.OK)
                    .results(Map.of("results", List.of(externalTransferDomainMapper.toDTO(externalTransfer))))
                    .notifications(List.of(NotificationDTO.builder()
                        .message(ShipmentServiceMessages.EXTERNAL_TRANSFER_PRODUCT_ADD_SUCCESS)
                        .statusCode(HttpStatus.OK.value())
                        .notificationType(NotificationType.SUCCESS.name())
                        .build()))
                    .build());
            })
            .onErrorResume(error -> {
                log.error("Not able to add a product {} error {}", addProductTransferCommandDTO,error.getMessage());
                return buildErrorResponse(error);
            });
    }
}
