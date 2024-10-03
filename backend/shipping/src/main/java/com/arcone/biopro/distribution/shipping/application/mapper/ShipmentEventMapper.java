package com.arcone.biopro.distribution.shipping.application.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentCompletedItemPayloadDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentCompletedItemProductPayloadDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentCompletedPayloadDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentItemPackedDTO;
import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class ShipmentEventMapper {

    public ShipmentCompletedEvent toShipmentCompletedEvent(ShipmentDetailResponseDTO dto , String facilityName) {

        return new ShipmentCompletedEvent(ShipmentCompletedPayloadDTO
            .builder()
            .shipmentId(dto.id())
            .orderNumber(dto.orderNumber())
            .externalOrderId(dto.externalId())
            .performedBy(dto.completedByEmployeeId())
            .locationCode(dto.locationCode())
            .locationName(facilityName)
            .customerCode(dto.shippingCustomerCode())
            .createDate(dto.createDate())
            .lineItems(buildLineItems(dto.items()))
            .build());
    }

    private List<ShipmentCompletedItemPayloadDTO> buildLineItems(List<ShipmentItemResponseDTO> responseItems){

        return   ofNullable(responseItems)
            .filter(items -> !items.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(shipmentItemResponseDTO -> ShipmentCompletedItemPayloadDTO
                .builder()
                .productFamily(shipmentItemResponseDTO.productFamily())
                .quantity(shipmentItemResponseDTO.quantity())
                .bloodType(shipmentItemResponseDTO.bloodType().name())
                .products(buildProductList(shipmentItemResponseDTO.packedItems()))
                .build()).toList();
    }

    private List<ShipmentCompletedItemProductPayloadDTO> buildProductList(List<ShipmentItemPackedDTO> packedItems){

        return   ofNullable(packedItems)
            .filter(items -> !items.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(itemPackedDTO -> ShipmentCompletedItemProductPayloadDTO
                .builder()
                .unitNumber(itemPackedDTO.unitNumber())
                .productCode(itemPackedDTO.productCode())
                .productFamily(itemPackedDTO.productFamily())
                .aboRh(itemPackedDTO.aboRh())
                .collectionDate(itemPackedDTO.collectionDate())
                .expirationDate(itemPackedDTO.expirationDate())
                .createDate(ZonedDateTime.now())
                .build()).toList();

    }
}
