package com.arcone.biopro.distribution.shippingservice.application.usecase;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentItemShortDateProductResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shippingservice.domain.model.Shipment;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItemShortDateProduct;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shippingservice.domain.service.ShipmentService;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderItemFulfilledMessage;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.ShortDateItem;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceUseCase implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;

    private final ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;



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
            .build();

        return shipmentRepository.save(shipment)
            .flatMap(savedShipment -> {
                return  Flux.fromStream(message.items().stream())
                    .flatMap(orderItemFulfilledMessage -> {
                        return shipmentItemRepository.save(toShipmentItem(orderItemFulfilledMessage,savedShipment.getId())).flatMap(shipmentItemSaved -> {
                            var shortDateProducts = orderItemFulfilledMessage.shortDateProducts();
                            if(shortDateProducts != null){
                                return Flux.fromStream(orderItemFulfilledMessage.shortDateProducts().stream()).flatMap(shortDateItem -> {
                                    return shipmentItemShortDateProductRepository.save(toShipmentItemShortDateProduct(shortDateItem,shipmentItemSaved.getId())).then(Mono.just(""));
                                }).collectList();
                            }
                            return Mono.empty();
                        }).then(Mono.just(""));
                    }).collectList();
            })
            .then(Mono.just(shipment));
    }

    private ShipmentItem toShipmentItem(OrderItemFulfilledMessage itemFulfilledMessage, Long shipmentId){
        return ShipmentItem.builder()
            .shipmentId(shipmentId)
            .quantity(itemFulfilledMessage.quantity())
            .bloodType(BloodType.valueOf(itemFulfilledMessage.bloodType()))
            .productFamily(itemFulfilledMessage.productFamily())
            .comments(itemFulfilledMessage.comments())
            .build();
    }

    private ShipmentItemShortDateProduct toShipmentItemShortDateProduct(ShortDateItem shortDateItem , Long shipmentItemId){

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

        log.debug("Fetching Shipment Items for Shipment ID {}",shipment.getId());

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

                    log.debug("Fetching Shipment Items Short Date for Shipment Item ID {}",shipmentItem.getId());

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

    private ShipmentItemShortDateProductResponseDTO toShipmentItemShortDateProductResponseDTO(ShipmentItemShortDateProduct shortDateProduct){

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
