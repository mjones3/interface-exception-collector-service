package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.RemoveProductResponseDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.VerifyProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RemoveItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemRemoved;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRemovedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.RemoveShipmentItemService;
import com.arcone.biopro.distribution.shipping.domain.service.SecondVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RemoveShipmentItemUseCase implements RemoveShipmentItemService {

    private final ShipmentItemPackedRepository shipmentItemPackedRepository;
    private final ShipmentMapper shipmentMapper;
    private final SecondVerificationService secondVerificationService;
    private final ShipmentItemRemovedRepository shipmentItemRemovedRepository;


    @Override
    public Mono<RuleResponseDTO> removeItem(RemoveItemRequest removeItemRequest) {
        return shipmentItemPackedRepository.findByUnitTobeRemoved(removeItemRequest.shipmentId()
                ,removeItemRequest.unitNumber(), removeItemRequest.productCode())
            .switchIfEmpty(Mono.defer(() -> {
                return secondVerificationService.resetVerification(removeItemRequest.shipmentId(), ShipmentServiceMessages.SECOND_VERIFICATION_UNIT_NOT_TOBE_REMOVED_ERROR);
            }))
            .flatMap(shipmentItemPacked -> removeItem(shipmentItemPacked,removeItemRequest))
            .flatMap(packedItem ->
                Mono.from(getNotificationDetailsByShipmentId(removeItemRequest,packedItem)).flatMap(shipmentItemResponseDTO -> Mono.just(RuleResponseDTO.builder()
                    .ruleCode(HttpStatus.OK)
                    .results(Map.of("results", List.of(shipmentItemResponseDTO)))
                    .build())))
            .onErrorResume(error -> {
                log.error("Failed on remove item {} , {} ", removeItemRequest, error.getMessage());
                return buildRemoveItemErrorResponse(error, removeItemRequest.shipmentId());
            });
    }

    @Override
    public Mono<RemoveProductResponseDTO> getNotificationDetailsByShipmentId(Long shipmentId) {
        var removedList = shipmentItemRemovedRepository.findAllByShipmentId(shipmentId).collectList();
        var tobeRemovedList = shipmentItemPackedRepository.listAllIneligibleByShipmentId(shipmentId).collectList();

        return Mono.zip(removedList,tobeRemovedList).flatMap(tuple -> Mono.just(RemoveProductResponseDTO
            .builder()
            .shipmentId(shipmentId)
            .removedItems(tuple.getT1().stream().map(shipmentMapper::toShipmentItemRemovedDTO).toList())
            .toBeRemovedItems(tuple.getT2().stream().map(shipmentMapper::toShipmentItemPackedDTO).toList())
            .build()));
    }

    private Mono<RemoveProductResponseDTO> getNotificationDetailsByShipmentId(RemoveItemRequest removeItemRequest , ShipmentItemPacked shipmentItemPacked) {
        var notificationDetails = getNotificationDetailsByShipmentId(removeItemRequest.shipmentId());
        var removedItem = Mono.just(shipmentMapper.toShipmentItemPackedDTO(shipmentItemPacked));
        return Mono.zip(notificationDetails,removedItem).flatMap(tuple -> Mono.just(RemoveProductResponseDTO
            .builder()
                .shipmentId(tuple.getT1().shipmentId())
                .removedItems(tuple.getT1().removedItems())
                .toBeRemovedItems(tuple.getT1().toBeRemovedItems())
                .removedItem(tuple.getT2())
            .build()));
    }

    private Mono<ShipmentItemPacked> removeItem(ShipmentItemPacked itemPacked, RemoveItemRequest removeItemRequest) {
        return shipmentItemPackedRepository.delete(itemPacked)
            .then(shipmentItemRemovedRepository.save(ShipmentItemRemoved
                .builder()
                    .removedByEmployeeId(removeItemRequest.employeeId())
                    .ineligibleStatus(itemPacked.getIneligibleStatus())
                    .shipmentId(removeItemRequest.shipmentId())
                    .productCode(itemPacked.getProductCode())
                    .unitNumber(itemPacked.getUnitNumber())
                .build()).flatMap(removeItem -> Mono.just(itemPacked)));
    }

    private Mono<RuleResponseDTO> buildRemoveItemErrorResponse(Throwable error , Long shipmentId) {
        return getNotificationDetailsByShipmentId(shipmentId).flatMap(details -> {
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
    }
}
