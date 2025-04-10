package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Inventory;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryNotification;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.InventoryVolume;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryVolumeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    default Inventory toInventoryModel(InventoryResponseDTO inventoryResponseDTO) {
        if(inventoryResponseDTO == null){
            return null;
        }
        return new Inventory(
                inventoryResponseDTO.id(),
                inventoryResponseDTO.locationCode(),
                inventoryResponseDTO.unitNumber(),
                inventoryResponseDTO.productCode(),
                inventoryResponseDTO.productDescription(),
                inventoryResponseDTO.expirationDate(),
                inventoryResponseDTO.aboRh(),
                inventoryResponseDTO.productFamily(),
                inventoryResponseDTO.collectionDate(),
                inventoryResponseDTO.storageLocation(),
                inventoryResponseDTO.createDate(),
                inventoryResponseDTO.modificationDate(),
                inventoryResponseDTO.weight(),
                toVolumeModel(inventoryResponseDTO.volumes())

        );
    }

    InventoryNotification toNotificationModel(InventoryNotificationDTO inventoryNotificationDTO);

    @Mapping(source = "inventoryResponseDTO" , target = "inventory")
    @Mapping(source = "inventoryNotificationsDTO" , target = "notifications")
    InventoryValidation toValidationModel(InventoryValidationResponseDTO inventoryValidationResponseDTO);

    List<InventoryVolume> toVolumeModel(List<InventoryVolumeDTO> inventoryVolumeDTO);

}
