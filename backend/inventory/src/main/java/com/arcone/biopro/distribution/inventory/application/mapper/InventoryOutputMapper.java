package com.arcone.biopro.distribution.inventory.application.mapper;

import com.arcone.biopro.distribution.inventory.application.dto.*;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.NotificationMessage;
import com.arcone.biopro.distribution.inventory.domain.service.TextConfigService;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Setter
@Mapper
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class InventoryOutputMapper {

    TextConfigService textConfigService;

    @Mapping(target = "unitNumber", source = "unitNumber.value")
    @Mapping(target = "productCode", source = "productCode.value")
    public abstract InventoryOutput toOutput(Inventory domain);

    @Mapping(target = "productFamily", source = "productFamily")
    @Mapping(target = "aboRh", source = "aboRh")
    @Mapping(target = "quantityAvailable", source = "quantity")
    @Mapping(target = "shortDateProducts", source = "aggregates")
    public abstract InventoryFamily toOutput(String productFamily, AboRhCriteria aboRh, Long quantity, List<InventoryAggregate> aggregates);

    public abstract GetAllAvailableInventoriesOutput toOutput(String location, List<InventoryFamily> inventories);

    @Mapping(target = "unitNumber", source = "inventory.unitNumber.value")
    @Mapping(target = "productCode", source = "inventory.productCode.value")
    @Mapping(target = "storageLocation", source = "inventory.storageLocation")
    @Mapping(target = "aboRh", source = "inventory.aboRh")
    public abstract Product toOutput(InventoryAggregate inventoryAggregate);

    @Mapping(target = "inventoryOutput", source = "inventory")
    @Mapping(target = "notificationMessages.message", expression = "java(toOutput(notificationMessage.message()))")
    public abstract ValidateInventoryOutput toValidateInventoryOutput(InventoryAggregate inventoryAggregate);

    @Mapping(target = "message", expression = "java(textConfigService.getText(notificationMessage.message()))")
    public abstract NotificationMessage toOutput(NotificationMessage notificationMessage);

    @Mapping(target = "inventoryOutput", ignore = true)
    @Mapping(target = "notificationMessages", expression = "java(java.util.List.of(toNotificationMessage(notificationType)))")
    public abstract ValidateInventoryOutput toOutput(MessageType notificationType);

    @Mapping(target = "name", expression = "java(notificationType.name())")
    @Mapping(target = "message", expression = "java(textConfigService.getText(notificationType.name()))")
    @Mapping(target = "action", expression = "java(notificationType.getAction().name())")
    @Mapping(target = "reason", ignore = true)
    public abstract NotificationMessage toNotificationMessage(MessageType notificationType);

    @Mapping(target = "inventory.unitNumber.value", source = "unitNumber")
    @Mapping(target = "inventory.productCode.value", source = "productCode")
    @Mapping(target = "inventory.shortDescription", source = "shortDescription")
    @Mapping(target = "inventory.expirationDate", source = "expirationDate")
    @Mapping(target = "inventory.collectionDate", source = "collectionDate")
    @Mapping(target = "inventory.isLicensed", source = "isLicensed")
    @Mapping(target = "inventory.weight", source = "weight")
    @Mapping(target = "inventory.location", source = "location")
    @Mapping(target = "inventory.productFamily", source = "productFamily")
    @Mapping(target = "inventory.aboRh", source = "aboRh")
    @Mapping(target = "inventory.id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "inventory.inventoryStatus", expression = "java(com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus.AVAILABLE)")
    @Mapping(target = "notificationMessages", ignore = true)
    public abstract InventoryAggregate toAggregate(InventoryInput input);

}
