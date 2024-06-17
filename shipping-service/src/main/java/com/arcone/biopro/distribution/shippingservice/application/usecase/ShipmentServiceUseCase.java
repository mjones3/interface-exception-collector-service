package com.arcone.biopro.distribution.shippingservice.application.usecase;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentItemShortDateProductResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shippingservice.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shippingservice.application.dto.PackItemRequest;
import com.arcone.biopro.distribution.shippingservice.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shippingservice.application.dto.ShipmentItemPackedDTO;
import com.arcone.biopro.distribution.shippingservice.domain.model.Shipment;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItemShortDateProduct;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.VisualInspection;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shippingservice.domain.service.ShipmentService;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderItemFulfilledMessage;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.ShortDateItem;
import com.arcone.biopro.distribution.shippingservice.infrastructure.service.InventoryRsocketClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceUseCase implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;
    private final InventoryRsocketClient inventoryRsocketClient;
    private final ShipmentItemPackedRepository shipmentItemPackedRepository;


    @Override
    @Transactional
    public Mono<Shipment> create(OrderFulfilledMessage message) {

        log.info("Creating Shipment For Order number {}", message.orderNumber());
        var shipment = Shipment.builder()
            .orderNumber(message.orderNumber())
            .status(ShipmentStatus.valueOf(message.status()))
            .priority(ShipmentPriority.valueOf(message.priority()))
            .deliveryType(message.deliveryType())
            .shippingDate(message.shippingDate())
            .priority(ShipmentPriority.valueOf(message.priority()))
            .shipmentMethod(message.shippingMethod())
            .customerCode(message.shippingCustomerCode())
            .customerName(message.shippingCustomerName())
            .locationCode(message.locationCode())
            .productCategory(message.productCategory())
            .addressLine1(message.customerAddressAddressLine1())
            .addressLine2(message.customerAddressAddressLine2())
            .city(message.customerAddressCity())
            .customerPhoneNumber(message.customerPhoneNumber())
            .country(message.customerAddressCountry())
            .countryCode(message.customerAddressCountryCode())
            .postalCode(message.customerAddressPostalCode())
            .state(message.customerAddressState())
            .district(message.customerAddressDistrict())
            .comments(message.comments())
            .departmentName(message.departmentName())
            .createdByEmployeeId("mock-employee-id")
            .build();

        return shipmentRepository.save(shipment)
            .flatMap(savedShipment -> {
                return Flux.fromStream(message.items().stream())
                    .flatMap(orderItemFulfilledMessage -> {
                        return shipmentItemRepository.save(toShipmentItem(orderItemFulfilledMessage, savedShipment.getId())).flatMap(shipmentItemSaved -> {
                            var shortDateProducts = orderItemFulfilledMessage.shortDateProducts();
                            if (shortDateProducts != null) {
                                return Flux.fromStream(orderItemFulfilledMessage.shortDateProducts().stream()).flatMap(shortDateItem -> {
                                    return shipmentItemShortDateProductRepository.save(toShipmentItemShortDateProduct(shortDateItem, shipmentItemSaved.getId())).then(Mono.just(""));
                                }).collectList();
                            }
                            return Mono.empty();
                        }).then(Mono.just(""));
                    }).collectList();
            })
            .then(Mono.just(shipment));
    }

    private ShipmentItem toShipmentItem(OrderItemFulfilledMessage itemFulfilledMessage, Long shipmentId) {
        return ShipmentItem.builder()
            .shipmentId(shipmentId)
            .quantity(itemFulfilledMessage.quantity())
            .bloodType(BloodType.valueOf(itemFulfilledMessage.bloodType()))
            .productFamily(itemFulfilledMessage.productFamily())
            .comments(itemFulfilledMessage.comments())
            .build();
    }

    private ShipmentItemShortDateProduct toShipmentItemShortDateProduct(ShortDateItem shortDateItem, Long shipmentItemId) {

        return ShipmentItemShortDateProduct.builder()
            .shipmentItemId(shipmentItemId)
            .unitNumber(shortDateItem.unitNumber())
            .productCode(shortDateItem.productCode())
            .storageLocation(shortDateItem.storageLocation())
            .build();
    }

    @WithSpan("listShipments")
    public Flux<ShipmentResponseDTO> listShipments() {
        log.info("Listing shipments.....");
        return shipmentRepository.findAll().switchIfEmpty(Flux.empty()).flatMap(shipment -> convertShipmentResponse(shipment));
    }

    @Override
    @WithSpan("packItem")
    @Transactional
    public Mono<RuleResponseDTO> packItem(PackItemRequest packItemRequest) {

        return validateInventory(packItemRequest)
            .flatMap(inventoryResponseDTO -> validateProductCriteria(packItemRequest, inventoryResponseDTO)
                .flatMap(shipmentItemPacked ->
                    Mono.from(getShipmentItemById(shipmentItemPacked.getShipmentItemId())).flatMap(shipmentItemResponseDTO -> Mono.just(RuleResponseDTO.builder()
                        .ruleCode(HttpStatus.OK)
                        .results(Map.of("results", List.of(shipmentItemResponseDTO)))
                        .build()))
                )
            )
            .onErrorResume(error -> {
                log.error("Failed on pack item {} , {} ", packItemRequest, error.getMessage());
                return buildPackErrorResponse(error.getMessage());
            });
    }

    private Mono<ShipmentItemResponseDTO> getShipmentItemById(Long shipmentItemId) {

        return shipmentItemRepository.findById(shipmentItemId)
            .switchIfEmpty(Mono.error(new RuntimeException("shipment-item-not-found.error")))
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
                    shipmentItemResponse.shortDateProducts().add(toShipmentItemShortDateProductResponseDTO(shortDateProduct));
                    return Mono.just(shortDateProduct);
                }).then(Mono.just(shipmentItemResponse));
            }).zipWith(shipmentItemPackedRepository.findAllByShipmentItemId(shipmentItemId).switchIfEmpty(Flux.empty()).collectList())
            .flatMap(tuple2 -> {
                tuple2.getT2().forEach(shipmentItemPacked -> tuple2.getT1().packedItems().add(ShipmentItemPackedDTO.builder()
                    .id(shipmentItemPacked.getId())
                    .aboRh(shipmentItemPacked.getAboRh())
                    .expirationDate(shipmentItemPacked.getExpirationDate())
                    .shipmentItemId(shipmentItemPacked.getShipmentItemId())
                    .productCode(shipmentItemPacked.getProductCode())
                    .unitNumber(shipmentItemPacked.getUnitNumber())
                    .productFamily(tuple2.getT1().productFamily())
                    .productDescription(shipmentItemPacked.getProductDescription())
                    .collectionDate(shipmentItemPacked.getCollectionDate())
                    .packedByEmployeeId(shipmentItemPacked.getPackedByEmployeeId())
                        .visualInspection(shipmentItemPacked.getVisualInspection())
                    .build()));

                return Mono.just(tuple2.getT1());
            });
    }

    private Mono<InventoryResponseDTO> validateInventory(PackItemRequest packItemRequest) {
        return inventoryRsocketClient.validateInventory(InventoryValidationRequest.builder()
            .productCode(packItemRequest.productCode())
            .locationCode(packItemRequest.locationCode())
            .unitNumber(packItemRequest.unitNumber()).build()).flatMap(inventoryValidationResponseDTO -> {
            if (inventoryValidationResponseDTO.inventoryResponseDTO() != null) {
                return Mono.just(inventoryValidationResponseDTO.inventoryResponseDTO());
            } else {
                return Mono.error(new RuntimeException(inventoryValidationResponseDTO.inventoryNotificationDTO().errorMessage()));
            }
        });
    }

    private Mono<ShipmentItemPacked> validateProductCriteria(PackItemRequest request, InventoryResponseDTO inventoryResponseDTO) {

        return shipmentItemRepository.findById(request.shipmentItemId())
            .switchIfEmpty(Mono.error(new RuntimeException("shipment-item-not-found.error")))
            .flatMap(shipmentItem -> {
                if (!shipmentItem.getProductFamily().equals(inventoryResponseDTO.productFamily())) {
                    log.error("Product Family does not match");
                    return Mono.error(new RuntimeException("product-criteria-family-does-not-match.error"));
                } else if (!inventoryResponseDTO.aboRh().contains(shipmentItem.getBloodType().name())) {
                    log.error("Blood Type does not match");
                    return Mono.error(new RuntimeException("product-criteria-blood-type-does-not-match.error"));
                } else if(!VisualInspection.SATISFACTORY.equals(request.visualInspection())){
                    return Mono.error(new RuntimeException("product-criteria-visual-inspection.error"));
                }

                return Mono.just(shipmentItem);
            }).zipWith(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(request.unitNumber(), request.productCode()))
            .flatMap(tuple2 -> {
                if(tuple2.getT2() > 0){
                    return Mono.error(new RuntimeException("product-is-already-used.error"));
                }
                return Mono.just(tuple2.getT1());
            }).zipWith(shipmentItemPackedRepository.countAllByShipmentItemId(request.shipmentItemId()))
            .flatMap(tuple2 -> {

                var total = tuple2.getT2() + 1;
                if (total > tuple2.getT1().getQuantity()) {
                    log.error("Quantity exceeded");
                    return Mono.error(new RuntimeException("product-criteria-quantity-exceeded.error"));
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
                            .visualInspection(VisualInspection.SATISFACTORY)
                            .build())
                        .flatMap(savedPacked -> Mono.just(savedPacked));
                }
            });
    }

    private Mono<RuleResponseDTO> buildPackErrorResponse(String errorMessage) {
        var notification = new NotificationDTO(
            HttpStatus.BAD_REQUEST.value(),
            "error",
            errorMessage
        );
        return Mono.just(RuleResponseDTO.builder()
            .ruleCode(HttpStatus.BAD_REQUEST)
            .notifications(List.of(notification))
            .build());
    }

    private Mono<ShipmentResponseDTO> convertShipmentResponse(Shipment shipment) {

        return Mono.just(ShipmentResponseDTO.builder()
            .id(shipment.getId())
            .orderNumber(shipment.getOrderNumber())
            .status(shipment.getStatus())
            .priority(shipment.getPriority())
            .createDate(shipment.getCreateDate())
            .build());
    }

    private Mono<ShipmentDetailResponseDTO> convertShipmentResponseDetail(Shipment shipment) {

        List<ShipmentItemResponseDTO> shipmentItemList = new ArrayList<>();

        log.debug("Fetching Shipment Items for Shipment ID {}", shipment.getId());

        return shipmentItemRepository.findAllByShipmentId(shipment.getId())
            .flatMap(shipmentItem -> {
                var shipmentItemResponse = ShipmentItemResponseDTO.builder()
                    .id(shipmentItem.getId())
                    .productFamily(shipmentItem.getProductFamily())
                    .quantity(shipmentItem.getQuantity())
                    .shipmentId(shipmentItem.getShipmentId())
                    .comments(shipmentItem.getComments())
                    .bloodType(shipmentItem.getBloodType())
                    .shortDateProducts(new ArrayList<>())
                    .comments(shipmentItem.getComments())
                    .build();
                shipmentItemList.add(shipmentItemResponse);

                log.debug("Fetching Shipment Items Short Date for Shipment Item ID {}", shipmentItem.getId());

                return Flux.from(shipmentItemShortDateProductRepository.findAllByShipmentItemId(shipmentItem.getId()).switchIfEmpty(Flux.empty())).flatMap(shortDateProduct -> {
                    shipmentItemResponse.shortDateProducts().add(toShipmentItemShortDateProductResponseDTO(shortDateProduct));
                    return Mono.just(shortDateProduct);
                }).collectList();
            })
            .then(Mono.empty())
            .then(Mono.just(ShipmentDetailResponseDTO.builder()
                .id(shipment.getId())
                .orderNumber(shipment.getOrderNumber())
                .status(shipment.getStatus())
                .priority(shipment.getPriority())
                .createDate(shipment.getCreateDate())
                .deliveryType(shipment.getDeliveryType())
                .shippingDate(shipment.getShippingDate())
                .shippingMethod(shipment.getShipmentMethod())
                .shippingCustomerCode(shipment.getCustomerCode())
                .shippingCustomerName(shipment.getCustomerName())
                .locationCode(shipment.getLocationCode())
                .productCategory(shipment.getProductCategory())
                .customerAddressAddressLine1(shipment.getAddressLine1())
                .customerAddressAddressLine2(shipment.getAddressLine2())
                .customerAddressCity(shipment.getCity())
                .customerPhoneNumber(shipment.getCustomerPhoneNumber())
                .customerAddressCountry(shipment.getCountry())
                .customerAddressCountryCode(shipment.getCountryCode())
                .customerAddressPostalCode(shipment.getPostalCode())
                .customerAddressState(shipment.getState())
                .customerAddressDistrict(shipment.getDistrict())
                .items(shipmentItemList)
                .build()));
    }

    private ShipmentItemShortDateProductResponseDTO toShipmentItemShortDateProductResponseDTO(ShipmentItemShortDateProduct shortDateProduct) {

        return ShipmentItemShortDateProductResponseDTO.builder()
            .id(shortDateProduct.getId())
            .shipmentItemId(shortDateProduct.getShipmentItemId())
            .unitNumber(shortDateProduct.getUnitNumber())
            .productCode(shortDateProduct.getProductCode())
            .storageLocation(shortDateProduct.getStorageLocation())
            .comments(shortDateProduct.getComments())
            .createDate(shortDateProduct.getCreateDate())
            .modificationDate(shortDateProduct.getModificationDate())
            .build();

    }

    @WithSpan("getShipmentById")
    public Mono<ShipmentDetailResponseDTO> getShipmentById(Long shipmentId) {
        log.info("getting shipment detail by ID {}.....", shipmentId);
        return shipmentRepository.findById(shipmentId).switchIfEmpty(Mono.empty()).flatMap(shipment -> convertShipmentResponseDetail(shipment));
    }
}
