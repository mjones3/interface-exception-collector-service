package com.arcone.biopro.distribution.eventbridge.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.InventoryUpdatedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.InventoryUpdatedOutboundPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class InventoryUpdatedOutboundMapper {

    public InventoryUpdatedOutboundPayload toDto(InventoryUpdatedOutbound inventoryUpdatedOutbound){
        return InventoryUpdatedOutboundPayload
            .builder()
            .updateType(inventoryUpdatedOutbound.getUpdateType())
            .unitNumber(inventoryUpdatedOutbound.getUnitNumber())
            .productCode(inventoryUpdatedOutbound.getProductCode())
            .productDescription(inventoryUpdatedOutbound.getProductDescription())
            .productFamily(inventoryUpdatedOutbound.getProductFamily())
            .bloodType(inventoryUpdatedOutbound.getBloodType())
            .expirationDate(inventoryUpdatedOutbound.getExpirationDate())
            .locationCode(inventoryUpdatedOutbound.getLocationCode())
            .storageLocation(inventoryUpdatedOutbound.getStorageLocation())
            .inventoryStatus(inventoryUpdatedOutbound.getInventoryStatus())
            .properties(inventoryUpdatedOutbound.getProperties())
            .inputProducts(inventoryUpdatedOutbound.getInputProducts())
            .build();

    }
}
