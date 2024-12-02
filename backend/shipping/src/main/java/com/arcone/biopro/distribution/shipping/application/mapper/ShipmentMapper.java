package com.arcone.biopro.distribution.shipping.application.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentItemShortDateProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentItemPackedDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentItemRemovedDTO;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemRemoved;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemShortDateProduct;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShortDateItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
public class ShipmentMapper {


    public ShipmentItemPackedDTO toShipmentItemPackedDTO(ShipmentItemPacked shipmentItemPacked){
        return ShipmentItemPackedDTO.builder()
            .id(shipmentItemPacked.getId())
            .aboRh(shipmentItemPacked.getAboRh())
            .expirationDate(shipmentItemPacked.getExpirationDate())
            .shipmentItemId(shipmentItemPacked.getShipmentItemId())
            .productCode(shipmentItemPacked.getProductCode())
            .unitNumber(shipmentItemPacked.getUnitNumber())
            .productFamily(shipmentItemPacked.getProductFamily())
            .productDescription(shipmentItemPacked.getProductDescription())
            .collectionDate(shipmentItemPacked.getCollectionDate())
            .packedByEmployeeId(shipmentItemPacked.getPackedByEmployeeId())
            .visualInspection(shipmentItemPacked.getVisualInspection())
            .secondVerification(shipmentItemPacked.getSecondVerification())
            .verifiedByEmployeeId(shipmentItemPacked.getVerifiedByEmployeeId())
            .verifiedDate(shipmentItemPacked.getVerificationDate())
            .ineligibleStatus(Optional.ofNullable(shipmentItemPacked.getIneligibleStatus()).map(ineligibleStatus -> ineligibleStatus.label).orElse(null))
            .ineligibleAction(shipmentItemPacked.getIneligibleAction())
            .ineligibleReason(shipmentItemPacked.getIneligibleReason())
            .ineligibleMessage(shipmentItemPacked.getIneligibleMessage())
            .ineligibleDetails(Optional.ofNullable(shipmentItemPacked.getIneligibleDetails()).map(details -> Arrays.asList(details.split(","))).orElse(null))

            .build();
    }

    public ShipmentItemShortDateProduct toShipmentItemShortDateProduct(ShortDateItem shortDateItem, Long shipmentItemId) {

        return ShipmentItemShortDateProduct.builder()
            .shipmentItemId(shipmentItemId)
            .unitNumber(shortDateItem.unitNumber())
            .productCode(shortDateItem.productCode())
            .storageLocation(shortDateItem.storageLocation())
            .build();
    }

    public ShipmentItemShortDateProductResponseDTO toShipmentItemShortDateProductResponseDTO(ShipmentItemShortDateProduct shortDateProduct) {

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

    public ShipmentItemRemovedDTO toShipmentItemRemovedDTO(ShipmentItemRemoved shipmentItemRemoved){
        return ShipmentItemRemovedDTO.builder()
            .id(shipmentItemRemoved.getId())
            .shipmentId(shipmentItemRemoved.getShipmentId())
            .productCode(shipmentItemRemoved.getProductCode())
            .unitNumber(shipmentItemRemoved.getUnitNumber())
            .productFamily(shipmentItemRemoved.getProductFamily())
            .removedDate(shipmentItemRemoved.getRemovedDate())
            .removedByEmployeeId(shipmentItemRemoved.getRemovedByEmployeeId())
            .ineligibleStatus(Optional.ofNullable(shipmentItemRemoved.getIneligibleStatus()).map(ineligibleStatus -> ineligibleStatus.label).orElse(null))
            .build();
    }
}
