package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.component.BarcodeGenerator;
import com.arcone.biopro.distribution.shipping.application.dto.PackingListLabelDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipFromDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipToDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentItemPackedDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShippingLabelDTO;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ShipmentLabelService;
import com.arcone.biopro.distribution.shipping.infrastructure.service.FacilityServiceMock;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentLabelServiceUseCase implements ShipmentLabelService {

    private final ShipmentRepository shipmentRepository;
    private final BarcodeGenerator barcodeGenerator;
    private final FacilityServiceMock facilityServiceMock;
    private final ShipmentItemRepository shipmentItemRepository;

    private final ShipmentItemPackedRepository shipmentItemPackedRepository;


    private static final String ADDRESS_COMPLEMENT_FORMAT = "%s, %s, %s";

    @Override
    @WithSpan("generatePackingListLabel")
    public Mono<PackingListLabelDTO> generatePackingListLabel(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR)))
            .flatMap(shipment -> {
                if(!ShipmentStatus.COMPLETED.equals(shipment.getStatus())){
                    return Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_OPEN_ERROR));
                }
                return Mono.from(facilityServiceMock.getFacilityId(shipment.getLocationCode())).flatMap(facilityDTO -> {
                    var packingListLabel = PackingListLabelDTO.builder()
                        .shipmentId(shipment.getId())
                        .packedItems(new ArrayList<>())
                        .orderNumber(shipment.getOrderNumber())
                        .orderIdBase64Barcode(barcodeGenerator.generateCode128BarcodeBase64(String.format("%d", shipment.getOrderNumber())))
                        .shipmentIdBase64Barcode(barcodeGenerator.generateCode128BarcodeBase64(String.format("%d",shipment.getId())))
                        .dateTimePacked(shipment.getCompleteDate())
                        .enteredBy(shipment.getCreatedByEmployeeId())
                        .packedBy(shipment.getCompletedByEmployeeId())
                        .labelStatus(shipment.getLabelStatus())
                        .shipmentType(shipment.getShipmentType())
                        .distributionComments(shipment.getComments())
                        .shipFrom(ShipFromDTO.builder()
                            .bloodCenterCode(facilityDTO.externalId())
                            .bloodCenterName(facilityDTO.name())
                            .bloodCenterAddressLine1(facilityDTO.addressLine1())
                            .bloodCenterAddressLine2(facilityDTO.addressLine2())
                            .bloodCenterAddressComplement(String.format(ADDRESS_COMPLEMENT_FORMAT , facilityDTO.city() , facilityDTO.state() , facilityDTO.postalCode()))
                            .build())
                        .shipTo(ShipToDTO.builder()
                            .customerCode(shipment.getCustomerCode())
                            .customerName(shipment.getCustomerName())
                            .department(shipment.getDepartmentName())
                            .addressLine1(shipment.getAddressLine1())
                            .addressLine2(shipment.getAddressLine2())
                            .addressComplement(String.format(ADDRESS_COMPLEMENT_FORMAT , shipment.getCity() , shipment.getState() , shipment.getPostalCode()))
                            .build())
                        .build();

                    return Mono.just(packingListLabel);
                });
            }).flatMap(packingListLabel -> Flux.from(shipmentItemRepository.findAllByShipmentId(packingListLabel.shipmentId()).switchIfEmpty(Flux.empty()).flatMap(shipmentItem -> Flux.from(shipmentItemPackedRepository.findAllByShipmentItemId(shipmentItem.getId()).switchIfEmpty(Flux.empty()).flatMap(shipmentItemPacked -> {
                packingListLabel.packedItems().add(ShipmentItemPackedDTO.builder()
                        .aboRh(shipmentItemPacked.getAboRh())
                        .expirationDate(shipmentItemPacked.getExpirationDate())
                        .shipmentItemId(shipmentItem.getId())
                        .productCode(shipmentItemPacked.getProductCode())
                        .unitNumber(shipmentItemPacked.getUnitNumber())
                        .productFamily(shipmentItem.getProductFamily())
                        .productDescription(shipmentItemPacked.getProductDescription())
                        .collectionDate(shipmentItemPacked.getCollectionDate())
                    .build());
                return Mono.just(shipmentItemPacked);
            })).collectList()).collectList())
       .then(Mono.just(packingListLabel))
                );
    }

    @Override
    @WithSpan("generateShippingLabel")
    public Mono<ShippingLabelDTO> generateShippingLabel(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR)))
            .flatMap(shipment -> {
                if (!ShipmentStatus.COMPLETED.equals(shipment.getStatus())) {
                    return Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_OPEN_ERROR));
                }

                return Mono.from(facilityServiceMock.getFacilityId(shipment.getLocationCode())).flatMap(facilityDTO -> {
                    var shippingLabel = ShippingLabelDTO.builder()
                        .shipmentId(shipment.getId())
                        .dateTimePacked(shipment.getCompleteDate())
                        .orderNumber(shipment.getOrderNumber())
                        .orderIdBase64Barcode(barcodeGenerator.generateCode128BarcodeBase64(String.format("%d", shipment.getOrderNumber())))
                        .shipmentIdBase64Barcode(barcodeGenerator.generateCode128BarcodeBase64(String.format("%d",shipment.getId())))
                        .shipFrom(ShipFromDTO.builder()
                            .bloodCenterCode(facilityDTO.externalId())
                            .bloodCenterName(facilityDTO.name())
                            .bloodCenterBase64Barcode(barcodeGenerator.generateCode128BarcodeBase64(facilityDTO.externalId()))
                            .phoneNumber(facilityDTO.properties().get("PHONE_NUMBER"))
                            .bloodCenterAddressLine1(facilityDTO.addressLine1())
                            .bloodCenterAddressLine2(facilityDTO.addressLine2())
                            .bloodCenterAddressComplement(String.format(ADDRESS_COMPLEMENT_FORMAT , facilityDTO.city() , facilityDTO.state() , facilityDTO.postalCode()))
                            .build())
                        .shipTo(ShipToDTO.builder()
                            .customerCode(shipment.getCustomerCode())
                            .customerName(shipment.getCustomerName())
                            .department(shipment.getDepartmentName())
                            .addressLine1(shipment.getAddressLine1())
                            .addressLine2(shipment.getAddressLine2())
                            .phoneNumber(shipment.getCustomerPhoneNumber())
                            .addressComplement(String.format(ADDRESS_COMPLEMENT_FORMAT , shipment.getCity() , shipment.getState() , shipment.getPostalCode()))
                            .build())
                        .build();

                    return Mono.just(shippingLabel);
                });
            });
    }
}
