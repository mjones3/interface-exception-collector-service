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
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.SecondVerification;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.VisualInspection;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import com.arcone.biopro.distribution.shipping.domain.service.PackItemService;
import com.arcone.biopro.distribution.shipping.domain.service.SecondVerificationService;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.springframework.util.CollectionUtils.isEmpty;

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
    private final SecondVerificationService secondVerificationUseCase;
    private final ShipmentRepository shipmentRepository;
    private static final String INTERNAL_TRANSFER_TYPE = "INTERNAL_TRANSFER";
    private static final String UNLABELED_STATUS = "UNLABELED";
    private static final String UNLABELED_NOTIFICATION_TYPE = "INVENTORY_IS_UNLABELED";
    private static final String QUARANTINED_NOTIFICATION_TYPE = "INVENTORY_IS_QUARANTINED";

    @Override
    @WithSpan("packItem")
    @Transactional
    public Mono<RuleResponseDTO> packItem(PackItemRequest packItemRequest) {
        var visualInspection = configService.findShippingVisualInspectionActive();
        var secondVerification = configService.findShippingSecondVerificationActive();
        var validateInventory = validateInventory(packItemRequest);
        return Mono.zip(validateInventory, visualInspection, secondVerification)
            .flatMap(tuple -> validateProductCriteria(packItemRequest, tuple.getT1(), tuple.getT2(), tuple.getT3()))
            .flatMap(this::resetSecondVerification)
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

    private Mono<InventoryValidationResponseDTO> validateInventory(PackItemRequest packItemRequest) {
        return shipmentRepository.findShipmentByItemId(packItemRequest.shipmentItemId()).flatMap(shipment -> {
                return inventoryRsocketClient.validateInventory(InventoryValidationRequest.builder()
                    .productCode(packItemRequest.productCode())
                    .locationCode(packItemRequest.locationCode())
                    .unitNumber(packItemRequest.unitNumber()).build()).flatMap(inventoryValidationResponseDTO -> {
                    if (inventoryValidationResponseDTO.inventoryResponseDTO() != null && (inventoryValidationResponseDTO.inventoryNotificationsDTO() == null || inventoryValidationResponseDTO.inventoryNotificationsDTO().isEmpty())) {
                        return Mono.just(inventoryValidationResponseDTO);
                    } else {
                        var internalTransferAllowedNotifications = this.getInternalTransferAllowedNotifications(shipment);
                        if (!internalTransferAllowedNotifications.isEmpty()) {
                            if (inventoryValidationResponseDTO.hasOnlyNotificationTypes(internalTransferAllowedNotifications)) {
                                var inventoryResponseDto = inventoryValidationResponseDTO.hasOnlyNotificationType(QUARANTINED_NOTIFICATION_TYPE) ? transformQuarantinedInventoryResponseDTO(inventoryValidationResponseDTO) : transformUnlabeledInventoryResponseDTO(inventoryValidationResponseDTO);
                                return Mono.just(inventoryResponseDto);
                            }

                            log.debug("Criteria for shipment {} is/are {} and this product has notifications {}", shipment.getId(), internalTransferAllowedNotifications, inventoryValidationResponseDTO.inventoryNotificationsDTO());
                            return Mono.error(new ProductValidationException(ShipmentServiceMessages.INVENTORY_VALIDATION_FAILED,
                                inventoryValidationResponseDTO.inventoryResponseDTO(),
                                inventoryValidationResponseDTO.inventoryNotificationsDTO().stream()
                                    .map(inventoryNotificationDTO ->
                                        NotificationDTO.builder()
                                            .notificationType(NotificationType.WARN.name())
                                            .statusCode(HttpStatus.BAD_REQUEST.value())
                                            .message(ShipmentServiceMessages.ORDER_CRITERIA_DOES_NOT_MATCH_ERROR)
                                            .name("ORDER_CRITERIA_DOES_NOT_MATCH_ERROR")
                                            .build()
                                    )
                                .toList()
                            ));
                        }

                        return Mono.error(new ProductValidationException(ShipmentServiceMessages.INVENTORY_VALIDATION_FAILED
                            , inventoryValidationResponseDTO.inventoryResponseDTO()
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
                                .build())
                            .toList()));
                    }
                });
            }
        );

    }

    private Set<String> getInternalTransferAllowedNotifications(final Shipment shipment) {
        if (!INTERNAL_TRANSFER_TYPE.equals(shipment.getShipmentType())) {
            return Collections.emptySet();
        }
        var allowedNotifications = new HashSet<String>();
        if (isTrue(shipment.getQuarantinedProducts())){
            allowedNotifications.add(QUARANTINED_NOTIFICATION_TYPE);
        }
        if (UNLABELED_STATUS.equals(shipment.getLabelStatus())){
            allowedNotifications.add(UNLABELED_NOTIFICATION_TYPE);
        }
        return allowedNotifications;
    }

    private Mono<ShipmentItemPacked> validateProductCriteria(PackItemRequest request, InventoryValidationResponseDTO inventoryValidationResponseDTO, Boolean visualInspectionFlag, Boolean secondVerificationFlag) {
        var visualInspectionActive = ofNullable(visualInspectionFlag).orElse(TRUE);
        var secondVerificationActive = ofNullable(secondVerificationFlag).orElse(FALSE);
        var inventoryResponseDTO = inventoryValidationResponseDTO.inventoryResponseDTO();

        return shipmentItemRepository.findById(request.shipmentItemId())
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_ITEM_NOT_FOUND_ERROR)))
            .flatMap(shipmentItem -> {
                return shipmentRepository.findById(shipmentItem.getShipmentId()).flatMap(shipment -> {
                        if (!shipmentItem.getProductFamily().equals(inventoryResponseDTO.productFamily())) {
                            log.error("Product Family does not match");
                            return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_CRITERIA_FAMILY_ERROR, List.of(NotificationDTO
                                .builder()
                                .notificationType(NotificationType.WARN.name())
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .message(ShipmentServiceMessages.PRODUCT_CRITERIA_FAMILY_ERROR)
                                .name("PRODUCT_CRITERIA_FAMILY_ERROR")
                                .build())));

                        } else if (!BloodType.ANY.equals(shipmentItem.getBloodType()) && !inventoryResponseDTO.aboRh().contains(shipmentItem.getBloodType().name())) {
                            log.error("Blood Type does not match");
                            return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_CRITERIA_BLOOD_TYPE_ERROR, List.of(NotificationDTO
                                .builder()
                                .name("PRODUCT_CRITERIA_BLOOD_TYPE_ERROR")
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .message(ShipmentServiceMessages.PRODUCT_CRITERIA_BLOOD_TYPE_ERROR)
                                .notificationType(NotificationType.WARN.name())
                                .build())));
                        } else if (!shipment.getProductCategory().equals(inventoryResponseDTO.temperatureCategory())) {
                            return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_CRITERIA_BLOOD_TYPE_ERROR, List.of(NotificationDTO
                                .builder()
                                .name("PRODUCT_CRITERIA_TEMPERATURE_CATEGORY_ERROR")
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .message(ShipmentServiceMessages.PRODUCT_CRITERIA_TEMPERATURE_CATEGORY_ERROR)
                                .notificationType(NotificationType.WARN.name())
                                .build())));
                        } else if (INTERNAL_TRANSFER_TYPE.equals(shipment.getShipmentType())
                            && isTrue(shipment.getQuarantinedProducts())
                            && (isEmpty(inventoryValidationResponseDTO.inventoryNotificationsDTO()) || !inventoryValidationResponseDTO.hasNotificationType(QUARANTINED_NOTIFICATION_TYPE))) {
                            return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_CRITERIA_ONLY_QUARANTINED_PRODUCT_ERROR, List.of(NotificationDTO
                                .builder()
                                .name("PRODUCT_CRITERIA_ONLY_QUARANTINED_PRODUCT_ERROR")
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .message(ShipmentServiceMessages.PRODUCT_CRITERIA_ONLY_QUARANTINED_PRODUCT_ERROR)
                                .notificationType(NotificationType.WARN.name())
                                .build())));
                        } else if (INTERNAL_TRANSFER_TYPE.equals(shipment.getShipmentType())
                            && UNLABELED_STATUS.equals(shipment.getLabelStatus())
                            && (isEmpty(inventoryValidationResponseDTO.inventoryNotificationsDTO()) || !inventoryValidationResponseDTO.hasNotificationType(UNLABELED_NOTIFICATION_TYPE))) {
                            return Mono.error(new ProductValidationException(ShipmentServiceMessages.SHIPMENT_UNLABELED_ERROR, List.of(NotificationDTO
                                .builder()
                                .name("SHIPMENT_UNLABELED_ERROR")
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .message(ShipmentServiceMessages.SHIPMENT_UNLABELED_ERROR)
                                .notificationType(NotificationType.WARN.name())
                                .build())));
                        }
                        return Mono.just(shipmentItem);
                    })
                    .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR)));
            }).zipWith(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(request.unitNumber(), request.productCode()))
            .flatMap(tuple2 -> {
                if (tuple2.getT2() > 0) {
                    return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_ALREADY_USED_ERROR, List.of(NotificationDTO
                        .builder()
                        .name("PRODUCT_ALREADY_USED_ERROR")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(ShipmentServiceMessages.PRODUCT_ALREADY_USED_ERROR)
                        .notificationType(NotificationType.WARN.name())
                        .build())));
                } else if (TRUE.equals(visualInspectionActive) && !VisualInspection.SATISFACTORY.equals(request.visualInspection())) {
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
                    return Mono.error(new ProductValidationException(ShipmentServiceMessages.PRODUCT_CRITERIA_QUANTITY_ERROR, List.of(NotificationDTO
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
                            .productStatus(inventoryResponseDTO.status())
                            .build())
                        .flatMap(Mono::just);
                }
            });
    }

    private Mono<RuleResponseDTO> buildPackErrorResponse(Throwable error) {
        if (error instanceof InventoryServiceNotAvailableException) {
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
        if (error instanceof ProductValidationException exception) {

            return configService.findVisualInspectionFailedDiscardReasons()
                .flatMap(reasonDomainMapper::flatMapToDto)
                .collectList()
                .map(reasons -> {
                    Map<String, List<?>> results = new HashMap<>();
                    if (((ProductValidationException) error).getInventoryResponseDTO() != null) {
                        results.put("inventory", List.of(((ProductValidationException) error).getInventoryResponseDTO()));
                    } else {
                        results.put("inventory", List.of(Collections.emptyList()));
                    }
                    results.put("reasons", reasons);
                    return RuleResponseDTO.builder()
                        .ruleCode(HttpStatus.BAD_REQUEST)
                        .results(results)
                        .notifications(exception.getNotifications())
                        .build();
                });
        } else {
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

    private Mono<ShipmentItemPacked> resetSecondVerification(ShipmentItemPacked shipmentItemPacked) {
        log.debug("Reset Second Verification {}", shipmentItemPacked);
        return shipmentItemRepository.findById(shipmentItemPacked.getShipmentItemId())
            .map(shipmentItem -> Shipment.builder().id(shipmentItem.getShipmentId()).build())
            .flatMap(secondVerificationUseCase::resetVerification)
            .thenReturn(shipmentItemPacked);

    }

    private InventoryValidationResponseDTO transformQuarantinedInventoryResponseDTO(InventoryValidationResponseDTO inventoryValidationResponseDTO) {

        return InventoryValidationResponseDTO.builder()
            .inventoryResponseDTO(transformInventoryResponseDTOWithStatus(inventoryValidationResponseDTO.inventoryResponseDTO(),"QUARANTINED"))
            .inventoryNotificationsDTO(inventoryValidationResponseDTO.inventoryNotificationsDTO())
            .build();

    }

    private InventoryValidationResponseDTO transformUnlabeledInventoryResponseDTO(InventoryValidationResponseDTO inventoryValidationResponseDTO) {

        return InventoryValidationResponseDTO.builder()
            .inventoryResponseDTO(transformInventoryResponseDTOWithStatus(inventoryValidationResponseDTO.inventoryResponseDTO(),"UNLABELED"))
            .inventoryNotificationsDTO(inventoryValidationResponseDTO.inventoryNotificationsDTO())
            .build();

    }

    private InventoryResponseDTO transformInventoryResponseDTOWithStatus(InventoryResponseDTO inventoryResponseDTO , String status) {
        return InventoryResponseDTO.builder()
            .unitNumber(inventoryResponseDTO.unitNumber())
            .productDescription(inventoryResponseDTO.productDescription())
            .productCode(inventoryResponseDTO.productCode())
            .temperatureCategory(inventoryResponseDTO.temperatureCategory())
            .productFamily(inventoryResponseDTO.productFamily())
            .expirationDate(inventoryResponseDTO.expirationDate())
            .expirationDate(inventoryResponseDTO.expirationDate())
            .aboRh(inventoryResponseDTO.aboRh())
            .isLabeled(inventoryResponseDTO.isLabeled())
            .isLicensed(inventoryResponseDTO.isLicensed())
            .status(status)
            .build();
    }
}
