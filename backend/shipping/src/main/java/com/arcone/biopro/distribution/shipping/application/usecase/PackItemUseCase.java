package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.PackItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.exception.ProductValidationException;
import com.arcone.biopro.distribution.shipping.application.mapper.ReasonDomainMapper;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.SecondVerification;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.VisualInspection;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import com.arcone.biopro.distribution.shipping.domain.service.PackItemService;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shipping.infrastructure.service.InventoryRsocketClient;
import com.arcone.biopro.distribution.shipping.infrastructure.service.errors.InventoryServiceNotAvailableException;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackItemUseCase implements PackItemService {

    private final ConfigService configService;
    private final ShipmentItemRepository shipmentItemRepository;
    private final ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;
    private final ShipmentItemPackedRepository shipmentItemPackedRepository;
    private final ShipmentMapper shipmentMapper;
    private final ReasonDomainMapper reasonDomainMapper;
    private final InventoryRsocketClient inventoryRsocketClient;

    @Override
    @WithSpan("packItem")
    @Transactional
    public Mono<RuleResponseDTO> packItem(PackItemRequest packItemRequest) {
        var visualInspection = configService.findShippingVisualInspectionActive();
        var secondVerification = configService.findShippingSecondVerificationActive();
        var validateInventory = validateInventory(packItemRequest);
        return Mono.zip(validateInventory,visualInspection,secondVerification)
            .flatMap(tuple -> validateProductCriteria(packItemRequest, tuple.getT1(), tuple.getT2() , tuple.getT3()))
            .flatMap(shipmentItemPacked ->
                Mono.from(getShipmentItemById(shipmentItemPacked.getShipmentItemId())).flatMap(shipmentItemResponseDTO -> Mono.just(RuleResponseDTO.builder()
                    .ruleCode(HttpStatus.OK)
                    .results(Map.of("results", List.of(shipmentItemResponseDTO)))
                    .build())))
            .onErrorResume(error -> {
                log.error("Failed on pack item {} , {} ", packItemRequest, error.getMessage());
                return buildPackErrorResponse(error);
            });
    }

    private Mono<ShipmentItemResponseDTO> getShipmentItemById(Long shipmentItemId) {

        return shipmentItemRepository.findById(shipmentItemId)
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_ITEM_NOT_FOUND_ERROR)))
            .flatMap(shipmentItem -> {
                var shipmentItemResponse = ShipmentItemResponseDTO.builder()
                    .id(shipmentItem.getId())
                    .productFamily(shipmentItem.getProductFamily())
                    .quantity(shipmentItem.getQuantity())
                    .shipmentId(shipmentItem.getShipmentId())
                    .comments(shipmentItem.getComments())
                    .bloodType(shipmentItem.getBloodType())
                    .shortDateProducts(new ArrayList<>())
                    .packedItems(new ArrayList<>())
                    .comments(shipmentItem.getComments())
                    .build();

                log.debug("Fetching Shipment Items Short Date for Shipment Item ID {}", shipmentItem.getId());

                return Flux.from(shipmentItemShortDateProductRepository.findAllByShipmentItemId(shipmentItem.getId()).switchIfEmpty(Flux.empty())).flatMap(shortDateProduct -> {
                    shipmentItemResponse.shortDateProducts().add(shipmentMapper.toShipmentItemShortDateProductResponseDTO(shortDateProduct));
                    return Mono.just(shortDateProduct);
                }).then(Mono.just(shipmentItemResponse));
            }).zipWith(shipmentItemPackedRepository.findAllByShipmentItemId(shipmentItemId).switchIfEmpty(Flux.empty()).collectList())
            .flatMap(tuple2 -> {
                tuple2.getT2().forEach(shipmentItemPacked -> tuple2.getT1().packedItems().add(shipmentMapper.toShipmentItemPackedDTO(shipmentItemPacked)));
                return Mono.just(tuple2.getT1());
            });
    }

    private Mono<InventoryResponseDTO> validateInventory(PackItemRequest packItemRequest) {
        return inventoryRsocketClient.validateInventory(InventoryValidationRequest.builder()
            .productCode(packItemRequest.productCode())
            .locationCode(packItemRequest.locationCode())
            .unitNumber(packItemRequest.unitNumber()).build()).flatMap(inventoryValidationResponseDTO -> {
            if (inventoryValidationResponseDTO.inventoryResponseDTO() != null && (inventoryValidationResponseDTO.inventoryNotificationsDTO() == null || inventoryValidationResponseDTO.inventoryNotificationsDTO().isEmpty())) {
                return Mono.just(inventoryValidationResponseDTO.inventoryResponseDTO());
            } else {
                return Mono.error(new ProductValidationException(ShipmentServiceMessages.INVENTORY_VALIDATION_FAILED
                    ,inventoryValidationResponseDTO.inventoryResponseDTO()
                    , inventoryValidationResponseDTO.inventoryNotificationsDTO().stream()
                    .map(inventoryNotificationDTO -> NotificationDTO
                        .builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .name(inventoryNotificationDTO.errorName())
                        .message(inventoryNotificationDTO.errorMessage())
                        .code(inventoryNotificationDTO.errorCode())
                        .action(inventoryNotificationDTO.action())
                        .notificationType(inventoryNotificationDTO.errorType())
                        .reason(inventoryNotificationDTO.reason())
                        .details(inventoryNotificationDTO.details())
                        .build() )
                    .toList()));
            }
        });
    }

    private Mono<ShipmentItemPacked> validateProductCriteria(PackItemRequest request, InventoryResponseDTO inventoryResponseDTO, Boolean visualInspectionFlag, Boolean secondVerificationFlag) {
        var visualInspectionActive = ofNullable(visualInspectionFlag).orElse(TRUE);
        var secondVerificationActive = ofNullable(secondVerificationFlag).orElse(FALSE);

        return shipmentItemRepository.findById(request.shipmentItemId())
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_ITEM_NOT_FOUND_ERROR)))
            .flatMap(shipmentItem -> {
                if (!shipmentItem.getProductFamily().equals(inventoryResponseDTO.productFamily())) {
                    log.error("Product Family does not match");
                    return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_CRITERIA_FAMILY_ERROR,List.of(NotificationDTO
                        .builder()
                        .notificationType(NotificationType.WARN.name())
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(ShipmentServiceMessages.PRODUCT_CRITERIA_FAMILY_ERROR)
                        .name("PRODUCT_CRITERIA_FAMILY_ERROR")
                        .build())));

                } else if (!inventoryResponseDTO.aboRh().contains(shipmentItem.getBloodType().name())) {
                    log.error("Blood Type does not match");
                    return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_CRITERIA_BLOOD_TYPE_ERROR , List.of(NotificationDTO
                        .builder()
                        .name("PRODUCT_CRITERIA_BLOOD_TYPE_ERROR")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(ShipmentServiceMessages.PRODUCT_CRITERIA_BLOOD_TYPE_ERROR)
                        .notificationType(NotificationType.WARN.name())
                        .build())));
                }
                return Mono.just(shipmentItem);
            }).zipWith(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(request.unitNumber(), request.productCode()))
            .flatMap(tuple2 -> {
                if(tuple2.getT2() > 0){
                    return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_ALREADY_USED_ERROR,List.of(NotificationDTO
                        .builder()
                        .name("PRODUCT_ALREADY_USED_ERROR")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(ShipmentServiceMessages.PRODUCT_ALREADY_USED_ERROR)
                        .notificationType(NotificationType.WARN.name())
                        .build())));
                } else if(TRUE.equals(visualInspectionActive) && !VisualInspection.SATISFACTORY.equals(request.visualInspection())){
                    return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_CRITERIA_VISUAL_INSPECTION_ERROR, inventoryResponseDTO, List.of(NotificationDTO
                        .builder()
                        .name("PRODUCT_CRITERIA_VISUAL_INSPECTION_ERROR")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(ShipmentServiceMessages.PRODUCT_CRITERIA_VISUAL_INSPECTION_ERROR)
                        .notificationType(NotificationType.WARN.name())
                        .build())));
                }
                return Mono.just(tuple2.getT1());
            }).zipWith(shipmentItemPackedRepository.countAllByShipmentItemId(request.shipmentItemId()))
            .flatMap(tuple2 -> {

                var total = tuple2.getT2() + 1;
                if (total > tuple2.getT1().getQuantity()) {
                    log.error("Quantity exceeded");
                    return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_CRITERIA_QUANTITY_ERROR,List.of(NotificationDTO
                        .builder()
                        .name("PRODUCT_CRITERIA_QUANTITY_ERROR")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(ShipmentServiceMessages.PRODUCT_CRITERIA_QUANTITY_ERROR)
                        .notificationType(NotificationType.WARN.name())
                        .build())));
                } else {
                    return shipmentItemPackedRepository.save(ShipmentItemPacked.builder()
                            .unitNumber(inventoryResponseDTO.unitNumber())
                            .productDescription(inventoryResponseDTO.productDescription())
                            .aboRh(inventoryResponseDTO.aboRh())
                            .productCode(inventoryResponseDTO.productCode())
                            .expirationDate(inventoryResponseDTO.expirationDate())
                            .collectionDate(inventoryResponseDTO.collectionDate())
                            .packedByEmployeeId(request.employeeId())
                            .shipmentItemId(tuple2.getT1().getId())
                            .productFamily(tuple2.getT1().getProductFamily())
                            .bloodType(tuple2.getT1().getBloodType())
                            .visualInspection(TRUE.equals(visualInspectionActive) ? VisualInspection.SATISFACTORY : VisualInspection.DISABLED)
                            .secondVerification(TRUE.equals(secondVerificationActive) ? SecondVerification.PENDING : SecondVerification.DISABLED)
                            .build())
                        .flatMap(Mono::just);
                }
            });
    }

    private Mono<RuleResponseDTO> buildPackErrorResponse(Throwable error) {
        if(error instanceof InventoryServiceNotAvailableException){
            return Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.BAD_REQUEST)
                .notifications(List.of(NotificationDTO
                    .builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .name("INVENTORY_SERVICE_IS_DOWN")
                    .message(error.getMessage())
                    .notificationType(NotificationType.SYSTEM.name())
                    .build()))
                .build());
        }
        if(error instanceof ProductValidationException exception){

            return configService.findVisualInspectionFailedDiscardReasons()
                .flatMap(reasonDomainMapper::flatMapToDto)
                .collectList()
                .map(reasons -> {
                    Map<String, List<?>> results  = new HashMap<>();
                    if(((ProductValidationException) error).getInventoryResponseDTO() != null ) {
                        results.put("inventory", List.of(((ProductValidationException) error).getInventoryResponseDTO()));
                    }else{
                        results.put("inventory", List.of(Collections.emptyList()));
                    }
                    results.put("reasons",reasons);
                    return RuleResponseDTO.builder()
                        .ruleCode(HttpStatus.BAD_REQUEST)
                        .results(results)
                        .notifications(exception.getNotifications())
                        .build();
                } );
        }else{
            return Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.BAD_REQUEST)
                .notifications(List.of(NotificationDTO
                    .builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .name(HttpStatus.BAD_REQUEST.name())
                    .message(error.getMessage())
                    .notificationType(NotificationType.WARN.name())
                    .build()))
                .build());
        }
    }
}
