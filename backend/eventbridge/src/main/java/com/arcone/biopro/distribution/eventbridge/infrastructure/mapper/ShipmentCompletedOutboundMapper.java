package com.arcone.biopro.distribution.eventbridge.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.ShipmentCompletedOutboundItem;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.ShipmentCompletedOutboundItemProduct;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.ShipmentCompletedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.ShipmentCompletedOutboundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class ShipmentCompletedOutboundMapper {

    public ShipmentCompletedOutboundPayload toDto(ShipmentCompletedOutbound shipmentCompletedOutbound){
        return ShipmentCompletedOutboundPayload
            .builder()
            .shipmentNumber(shipmentCompletedOutbound.getShipmentId())
            .externalOrderId(shipmentCompletedOutbound.getExternalId())
            .customerCode(shipmentCompletedOutbound.getShipmentCustomer().customerCode())
            .customerType(shipmentCompletedOutbound.getShipmentCustomer().customerType())
            .shipmentDate(shipmentCompletedOutbound.getShipmentDate())
            .quantityShipped(shipmentCompletedOutbound.getQuantityShipped())
            .shipmentLocationCode(shipmentCompletedOutbound.getShipmentLocation().shipmentLocationCode())
            .shipmentLocationName(shipmentCompletedOutbound.getShipmentLocation().shipmentLocationName())
            .lineItems(ofNullable(shipmentCompletedOutbound.getLineItems())
                .filter(lineItems -> !lineItems.isEmpty())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(lineItem -> ShipmentCompletedOutboundItem
                    .builder()
                    .productFamily(lineItem.getProductFamily())
                    .qtyOrdered(lineItem.getQuantityOrdered())
                    .qtyFilled(lineItem.getQuantityFilled())
                    .products(ofNullable(lineItem.getProducts())
                        .filter(lineItems -> !lineItems.isEmpty())
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(itemProduct -> ShipmentCompletedOutboundItemProduct
                            .builder()
                            .productCode(itemProduct.getProductCode())
                            .bloodType(itemProduct.getBloodType())
                            .unitNumber(itemProduct.getUnitNumber())
                            .collectionDate(itemProduct.getCollectionDate())
                            .expirationDate(itemProduct.getExpirationDate())
                            .build())
                        .toList()
                    )
                    .build())
                .toList())
            .services(ofNullable(shipmentCompletedOutbound.getServices())
                .filter(services -> !services.isEmpty())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(service -> ShipmentCompletedOutboundService
                    .builder()
                    .serviceItemCode(service.code())
                    .quantity(service.quantity())
                    .build())
                .toList())
            .build();

    }
}
