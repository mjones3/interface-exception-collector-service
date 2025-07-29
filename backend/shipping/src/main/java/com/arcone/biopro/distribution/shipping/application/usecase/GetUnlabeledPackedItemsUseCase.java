package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.GetUnlabeledPackedItemsRequest;
import com.arcone.biopro.distribution.shipping.application.dto.GetUnlabeledProductsRequest;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.exception.ProductValidationException;
import com.arcone.biopro.distribution.shipping.application.mapper.ProductResponseMapper;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.GetUnlabeledPackedItemsService;
import com.arcone.biopro.distribution.shipping.domain.service.GetUnlabeledProductsService;
import com.arcone.biopro.distribution.shipping.domain.service.SecondVerificationService;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationByUnitNumberRequest;
import com.arcone.biopro.distribution.shipping.infrastructure.service.InventoryRsocketClient;
import com.arcone.biopro.distribution.shipping.infrastructure.service.errors.InventoryServiceNotAvailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetUnlabeledPackedItemsUseCase implements GetUnlabeledPackedItemsService {


    private final ShipmentItemPackedRepository shipmentItemPackedRepository;
    private final ShipmentRepository shipmentRepository;
    private final ProductResponseMapper productResponseMapper;
    private static final String INTERNAL_TRANSFER_TYPE = "INTERNAL_TRANSFER";
    private static final String UNLABELED_STATUS = "UNLABELED";
    private final SecondVerificationService secondVerificationService;


    @Override
    public Mono<RuleResponseDTO> getUnlabeledPackedItems(GetUnlabeledPackedItemsRequest getUnlabeledPackedItemsRequest) {
        return shipmentRepository.findById(getUnlabeledPackedItemsRequest.shipmentId())
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR)))
            .flatMap(shipment -> {
                if (!UNLABELED_STATUS.equals(shipment.getLabelStatus())) {
                    return Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_LABEL_STATUS_ERROR));
                }

                if (!INTERNAL_TRANSFER_TYPE.equals(shipment.getShipmentType())) {
                    return Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_TYPE_NOT_MATCH_ERROR));
                }

                return shipmentItemPackedRepository.listAllPendingVerificationByShipmentIdAndUnitNumber(getUnlabeledPackedItemsRequest.shipmentId(), getUnlabeledPackedItemsRequest.unitNumber())
                    .switchIfEmpty(Mono.defer(() -> {
                        return secondVerificationService.resetVerification(shipment.getId(),ShipmentServiceMessages.SECOND_VERIFICATION_UNIT_NOT_PACKED_ERROR);
                    }))
                    .collectList()
                    .map(productResponseMapper::toProductResponseDTO)
                    .map(productList -> RuleResponseDTO.builder()
                        .ruleCode(HttpStatus.OK)
                        .results(Map.of("results", List.of(productList)))
                        .build());

            }).onErrorResume(error -> {
                log.error("Not able to getUnlabeled Packed Items error", error);
                return secondVerificationService.getVerificationDetailsByShipmentId(getUnlabeledPackedItemsRequest.shipmentId())
                    .flatMap(details -> {
                    return Mono.just(RuleResponseDTO.builder()
                        .ruleCode(HttpStatus.BAD_REQUEST)
                        .results(Map.of("results", List.of(details)))
                        .notifications(List.of(NotificationDTO
                            .builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .name(HttpStatus.BAD_REQUEST.name())
                            .message(error.getMessage())
                            .notificationType(NotificationType.WARN.name())
                            .build()))
                        .build());
                });
            });
    }
}
