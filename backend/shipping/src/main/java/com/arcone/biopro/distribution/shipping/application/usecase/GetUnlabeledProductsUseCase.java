package com.arcone.biopro.distribution.shipping.application.usecase;

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
import com.arcone.biopro.distribution.shipping.domain.service.GetUnlabeledProductsService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class GetUnlabeledProductsUseCase implements GetUnlabeledProductsService {


    private final ShipmentItemRepository shipmentItemRepository;
    private final ShipmentItemPackedRepository shipmentItemPackedRepository;
    private final InventoryRsocketClient inventoryRsocketClient;
    private final ShipmentRepository shipmentRepository;
    private final ProductResponseMapper productResponseMapper;
    private static final String INTERNAL_TRANSFER_TYPE = "INTERNAL_TRANSFER";
    private static final String UNLABELED_STATUS = "UNLABELED";


    @Override
    public Mono<RuleResponseDTO> getUnlabeledProducts(GetUnlabeledProductsRequest getUnlabeledProductsRequest) {

        return shipmentItemRepository.findById(getUnlabeledProductsRequest.shipmentItemId())
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_ITEM_NOT_FOUND_ERROR)))
            .flatMap(shipmentItem -> {
                return shipmentRepository.findById(shipmentItem.getId())
                    .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR)))
                    .zipWith(shipmentItemPackedRepository.listAllByShipmentId(shipmentItem.getShipmentId()).switchIfEmpty(Flux.empty()).collectList())
                    .flatMap(tuple -> {
                        if(!UNLABELED_STATUS.equals(tuple.getT1().getLabelStatus())) {
                            return Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_LABEL_STATUS_ERROR));
                        }

                        if(!INTERNAL_TRANSFER_TYPE.equals(tuple.getT1().getShipmentType())) {
                            return Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_TYPE_NOT_MATCH_ERROR));
                        }

                        return buildAvailableInventoryResponse(getUnlabeledProductsRequest,shipmentItem,tuple.getT1(),tuple.getT2());
                    });
            }).onErrorResume(error -> {
                log.error("Not able to getUnlabeledProducts error", error);
                return buildPackErrorResponse(error);
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
        }else if(error instanceof ProductValidationException exception) {
                Map<String, List<?>> results = new HashMap<>();
                if (((ProductValidationException) error).getInventoryResponseDTO() != null) {
                    results.put("inventory", List.of(((ProductValidationException) error).getInventoryResponseDTO()));
                } else {
                    results.put("inventory", List.of(Collections.emptyList()));
                }

                return Mono.just(RuleResponseDTO.builder()
                    .ruleCode(HttpStatus.BAD_REQUEST)
                    .results(results)
                    .notifications(exception.getNotifications())
                    .build());

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

    private Mono<RuleResponseDTO> buildAvailableInventoryResponse(GetUnlabeledProductsRequest getUnlabeledProductsRequest , ShipmentItem shipmentItem , Shipment shipment , List<ShipmentItemPacked> packedList) {
        return applyIneligibleCriteria(getUnlabeledProductsRequest,shipment)
            .collectList()
            .map(ineligibleList -> applyOrderCriteria(ineligibleList,shipment,shipmentItem))
            .map(this::applyUnlabeledCriteria)
            .map(unlabeledList -> applyProductAlreadySelectedCriteria(unlabeledList,packedList))
            .map(productResponseMapper::toResponseDTO)
            .map(productList -> RuleResponseDTO.builder()
                .ruleCode(HttpStatus.OK)
                .results(Map.of("results", List.of(productList)))
                .build());

    }

    private Flux<InventoryResponseDTO> applyIneligibleCriteria(GetUnlabeledProductsRequest getUnlabeledProductsRequest, Shipment shipment){

        var ineligibleList = new ArrayList<>(List.of("INVENTORY_NOT_FOUND_IN_LOCATION", "INVENTORY_IS_SHIPPED"
            , "INVENTORY_IS_UNSUITABLE", "INVENTORY_IS_DISCARDED"
            , "INVENTORY_IS_PACKED", "INVENTORY_NOT_EXIST"));

        if(shipment.getQuarantinedProducts() != null && !shipment.getQuarantinedProducts()){
            ineligibleList.add("INVENTORY_IS_QUARANTINED");
        }

        return inventoryRsocketClient.validateInventoryByUnitNumber(InventoryValidationByUnitNumberRequest
                .builder()
                .unitNumber(getUnlabeledProductsRequest.unitNumber())
                .locationCode(getUnlabeledProductsRequest.locationCode())
                .build())
            .filter(inventoryValidationResponseDTO -> inventoryValidationResponseDTO.inventoryNotificationsDTO() != null && !inventoryValidationResponseDTO.inventoryNotificationsDTO().isEmpty())
            .flatMap(inventoryValidationResponseDTO -> {
                var firstMarch = CollectionUtils.findFirstMatch(ineligibleList,inventoryValidationResponseDTO.inventoryNotificationsDTO().stream().map(InventoryNotificationDTO::errorName).toList());
                if(firstMarch != null){
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
                return Mono.just(inventoryValidationResponseDTO);
            })
            .filter(inventoryValidationResponseDTO -> Collections.disjoint(ineligibleList,inventoryValidationResponseDTO.inventoryNotificationsDTO().stream()
                .map(InventoryNotificationDTO::errorName).toList()))
            .switchIfEmpty(Flux.error(new RuntimeException(ShipmentServiceMessages.UNIT_DOES_NOT_EXIST_ERROR)))
            .flatMap(inventoryValidationResponseDTO -> Mono.just(inventoryValidationResponseDTO.inventoryResponseDTO()));
    }

    private List<InventoryResponseDTO> applyOrderCriteria(List<InventoryResponseDTO> inventoryResponseDTOList, Shipment shipment , ShipmentItem shipmentItem){
        var fielterdList = inventoryResponseDTOList.stream()
            .filter(inventoryResponseDTO -> {
                return inventoryResponseDTO.productFamily().equals(shipmentItem.getProductFamily())
                    && inventoryResponseDTO.temperatureCategory().equals(shipment.getProductCategory())
                    && (BloodType.ANY.equals(shipmentItem.getBloodType()) || inventoryResponseDTO.aboRh().contains(shipmentItem.getBloodType().name()));
            })
            .toList();

        if(fielterdList.isEmpty()){
            throw new ProductValidationException(ShipmentServiceMessages.ORDER_CRITERIA_DOES_NOT_MATCH_ERROR, List.of(NotificationDTO
                .builder()
                .notificationType(NotificationType.WARN.name())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ShipmentServiceMessages.ORDER_CRITERIA_DOES_NOT_MATCH_ERROR)
                .name("ORDER_CRITERIA_DOES_NOT_MATCH_ERROR")
                .build()));
        }
        return fielterdList;
    }

    private List<InventoryResponseDTO> applyUnlabeledCriteria(List<InventoryResponseDTO> inventoryResponseDTOList){
        var filteredList =  inventoryResponseDTOList.stream()
            .filter(inventoryResponseDTO -> !inventoryResponseDTO.isLabeled())
            .toList();

        if(filteredList.isEmpty()){
            throw new ProductValidationException(ShipmentServiceMessages.INVENTORY_LABELED_ERROR, List.of(NotificationDTO
                .builder()
                .notificationType(NotificationType.WARN.name())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ShipmentServiceMessages.INVENTORY_LABELED_ERROR)
                .name("INVENTORY_LABELED_ERROR")
                .build()));
        }
        return filteredList;

    }

    private List<InventoryResponseDTO> applyProductAlreadySelectedCriteria(List<InventoryResponseDTO> inventoryResponseDTOList , List<ShipmentItemPacked> packedList) {

        var packedItemsKey = packedList.stream()
            .map(shipmentItemPacked -> shipmentItemPacked.getUnitNumber() + ":" + shipmentItemPacked.getProductCode())
            .toList();

        var filteredList =  inventoryResponseDTOList.stream()
            .filter(inventoryResponseDTO -> !packedItemsKey.contains(inventoryResponseDTO.unitNumber() + ":" + inventoryResponseDTO.productCode()))
            .toList();

        if (filteredList.isEmpty()) {
            throw new ProductValidationException(ShipmentServiceMessages.ALL_PRODUCTS_SELECTED_ERROR, List.of(NotificationDTO
                .builder()
                .notificationType(NotificationType.CAUTION.name())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ShipmentServiceMessages.ALL_PRODUCTS_SELECTED_ERROR)
                .name("ALL_PRODUCTS_SELECTED_ERROR")
                .build()));
        }
        return filteredList;
    }
}
