package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCreatedEvent;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import com.arcone.biopro.distribution.shipping.domain.service.ShipmentService;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.OrderItemFulfilledMessage;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceUseCase implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;

    private final ShipmentItemPackedRepository shipmentItemPackedRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ConfigService configService;

    private final ShipmentMapper shipmentMapper;

    @Override
    @Transactional
    public Mono<Shipment> create(OrderFulfilledMessage message) {

        log.info("Creating Shipment For Order number {}", message.orderNumber());
        var shipment = Shipment.builder()
            .orderNumber(message.orderNumber())
            .externalId(message.externalId())
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
            .departmentCode(message.departmentCode())
            .createdByEmployeeId("mock-employee-id")
            .build();


        return shipmentRepository.save(shipment)
            .flatMap(savedShipment -> Flux.fromStream(message.items().stream())
                .flatMap(orderItemFulfilledMessage -> shipmentItemRepository.save(toShipmentItem(orderItemFulfilledMessage, savedShipment.getId())).flatMap(shipmentItemSaved -> {
                    var shortDateProducts = orderItemFulfilledMessage.shortDateProducts();
                    if (shortDateProducts != null) {
                        return Flux.fromStream(orderItemFulfilledMessage.shortDateProducts().stream()).flatMap(shortDateItem -> shipmentItemShortDateProductRepository.save(shipmentMapper.toShipmentItemShortDateProduct(shortDateItem, shipmentItemSaved.getId())).then(Mono.just(""))).collectList();
                    }
                    return Mono.empty();
                }).then(Mono.just(""))).collectList())
            .then(Mono.just(shipment))
            .doOnSuccess(this::publishShipmentCreatedEvent);
    }

    private void publishShipmentCreatedEvent(Shipment shipment) {
        log.info("Publishing Shipment Created Event {}",shipment);
        applicationEventPublisher.publishEvent(new ShipmentCreatedEvent(shipment));
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



    @WithSpan("listShipments")
    public Flux<ShipmentResponseDTO> listShipments() {
        log.info("Listing shipments.....");
        return shipmentRepository.findAll().switchIfEmpty(Flux.empty()).flatMap(this::convertShipmentResponse);
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

    private Mono<ShipmentDetailResponseDTO> convertShipmentResponseDetail(Shipment shipment, Boolean checkDigitFlag , Boolean visualInspectionFlag , Boolean secondVerificationFlag) {
        log.debug("Fetching Shipment Items for Shipment ID {}", shipment.getId());
        var checkDigitActive = ofNullable(checkDigitFlag).orElse(TRUE);
        var visualInspectionActive = ofNullable(visualInspectionFlag).orElse(TRUE);
        var secondVerificationActive = ofNullable(secondVerificationFlag).orElse(FALSE);
        var shipmentItemList = new ArrayList<ShipmentItemResponseDTO>();
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
                    .packedItems(new ArrayList<>())
                    .comments(shipmentItem.getComments())
                    .build();
                shipmentItemList.add(shipmentItemResponse);
                return Mono.just(shipmentItemResponse);

            }).flatMap(shipmentItemResponseDTO -> Flux.from(shipmentItemShortDateProductRepository.findAllByShipmentItemId(shipmentItemResponseDTO.id()).switchIfEmpty(Flux.empty())).flatMap(shortDateProduct -> {
                shipmentItemResponseDTO.shortDateProducts().add(shipmentMapper.toShipmentItemShortDateProductResponseDTO(shortDateProduct));
                return Mono.just(shortDateProduct);
            }).then(Mono.just(shipmentItemResponseDTO))) .flatMap(shipmentItemResponseDTO -> Flux.from(shipmentItemPackedRepository.findAllByShipmentItemId(shipmentItemResponseDTO.id())).switchIfEmpty(Flux.empty()).flatMap(itemPacked -> {
                shipmentItemResponseDTO.packedItems().add(shipmentMapper.toShipmentItemPackedDTO(itemPacked));
                return Mono.just(itemPacked);
            }).then(Mono.just(shipmentItemResponseDTO)))
            .then(Mono.empty())
            .then(Mono.just(ShipmentDetailResponseDTO.builder()
                .id(shipment.getId())
                .orderNumber(shipment.getOrderNumber())
                .externalId(shipment.getExternalId())
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
                .completeDate(shipment.getCompleteDate())
                .completedByEmployeeId(shipment.getCompletedByEmployeeId())
                .comments(shipment.getComments())
                .departmentCode(shipment.getDepartmentCode())
                .departmentName(shipment.getDepartmentName())
                .items(shipmentItemList)
                .checkDigitActive(checkDigitActive)
                .visualInspectionActive(visualInspectionActive)
                .secondVerificationActive(secondVerificationActive)
                .build()));
    }

    @WithSpan("getShipmentById")
    public Mono<ShipmentDetailResponseDTO> getShipmentById(Long shipmentId) {
        log.info("getting shipment detail by ID {}.....", shipmentId);
        var shipmentMono = shipmentRepository.findById(shipmentId).switchIfEmpty(Mono.empty());
        var checkDigitLookupMono = configService.findShippingCheckDigitActive();
        var visualInspection = configService.findShippingVisualInspectionActive();
        var secondVerification = configService.findShippingSecondVerificationActive();
        return Mono.zip(shipmentMono, checkDigitLookupMono , visualInspection , secondVerification)
            .flatMap(tuple -> this.convertShipmentResponseDetail(tuple.getT1(), tuple.getT2() , tuple.getT3() , tuple.getT4()));
    }

}
