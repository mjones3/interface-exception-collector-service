package com.arcone.biopro.distribution.irradiation.application.mapper;

import com.arcone.biopro.distribution.irradiation.BioProConstants;
import com.arcone.biopro.distribution.irradiation.application.dto.*;
import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.InputProduct;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.NotificationMessage;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.Volume;
import com.arcone.biopro.distribution.irradiation.domain.service.TextConfigService;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Setter
@Mapper(imports = {InventoryStatus.class})
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class InventoryOutputMapper {

    TextConfigService textConfigService;

    @Mapping(target = "unitNumber", source = "unitNumber.value")
    @Mapping(target = "productCode", source = "productCode.value")
    @Mapping(target = "location", source = "inventoryLocation")
    @Mapping(target = "productDescription", source = "shortDescription")
    public abstract InventoryOutput toOutput(Inventory domain);

    @Mapping(target = "unitNumber", source = "domain.unitNumber.value")
    @Mapping(target = "productCode", source = "domain.productCode.value")
    @Mapping(target = "location", source = "domain.inventoryLocation")
    @Mapping(target = "productDescription", source = "domain.shortDescription")
    @Mapping(target = "expired", source = "isExpired")
    public abstract InventoryOutput toOutput(Inventory domain, Boolean isExpired);

    @Mapping(target = "productFamily", source = "productFamily")
    @Mapping(target = "aboRh", source = "aboRh")
    @Mapping(target = "quantityAvailable", source = "quantity")
    @Mapping(target = "shortDateProducts", source = "aggregates")
    public abstract InventoryFamily toOutput(String productFamily, AboRhCriteria aboRh, Long quantity, List<InventoryAggregate> aggregates);

    public abstract GetAllAvailableInventoriesOutput toOutput(String location, List<InventoryFamily> inventories);

    @Mapping(target = "unitNumber", source = "irradiation.unitNumber.value")
    @Mapping(target = "productCode", source = "irradiation.productCode.value")
    @Mapping(target = "storageLocation", expression = "java(mapStorageLocation(inventoryAggregate.getInventory()))")
    @Mapping(target = "aboRh", source = "irradiation.aboRh")
    protected abstract Product toOutput(InventoryAggregate inventoryAggregate);

    protected String mapStorageLocation(Inventory inventory) {
        if (inventory.getDeviceStored() == null) {
            return null;
        } else if (inventory.getStorageLocation() == null) {
            return inventory.getDeviceStored();
        } else {
            return inventory.getDeviceStored() + " " + inventory.getStorageLocation();
        }
    }

    @Mapping(target = "inventoryOutput", source = "irradiation")
    @Mapping(target = "notificationMessages.message", expression = "java(toOutput(notificationMessage.message()))")
    public abstract ValidateInventoryOutput toValidateInventoryOutput(InventoryAggregate inventoryAggregate);

    @Mapping(target = "message", expression = "java(textConfigService.getText(notificationMessage.name(), notificationMessage.message()))")
    @Mapping(target = "details", expression = "java(toDetails(notificationMessage.details(), notificationMessage.name()))")
    protected abstract NotificationMessage toOutput(NotificationMessage notificationMessage);

    @Mapping(target = "inventoryOutput", ignore = true)
    @Mapping(target = "notificationMessages", expression = "java(java.util.List.of(toNotificationMessage(notificationType)))")
    public abstract ValidateInventoryOutput toOutput(MessageType notificationType);

    @Mapping(target = "name", expression = "java(notificationType.name())")
    @Mapping(target = "message", expression = "java(textConfigService.getText(notificationType.name(), notificationType.name()))")
    @Mapping(target = "action", expression = "java(notificationType.getAction().name())")
    @Mapping(target = "reason", ignore = true)
    @Mapping(target = "details", ignore = true)
    protected abstract NotificationMessage toNotificationMessage(MessageType notificationType);

    @Mapping(target = "irradiation.unitNumber.value", source = "unitNumber")
    @Mapping(target = "irradiation.productCode.value", source = "productCode")
    @Mapping(target = "irradiation.shortDescription", source = "shortDescription")
    @Mapping(target = "irradiation.expirationDate", source = "expirationDate")
    @Mapping(target = "irradiation.collectionDate", source = "collectionDate")
    @Mapping(target = "irradiation.isLicensed", source = "isLicensed")
    @Mapping(target = "irradiation.weight", source = "weight")
    @Mapping(target = "irradiation.inventoryLocation", source = "inventoryLocation")
    @Mapping(target = "irradiation.productFamily", source = "productFamily")
    @Mapping(target = "irradiation.aboRh", source = "aboRh")
    @Mapping(target = "irradiation.id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "irradiation.inventoryStatus", expression = "java(InventoryStatus.AVAILABLE)")
    @Mapping(target = "notificationMessages", ignore = true)
    @Mapping(target = "irradiation.isLabeled", expression = "java(java.lang.Boolean.TRUE)")
    @Mapping(target = "properties", ignore = true)
    public abstract InventoryAggregate toAggregate(InventoryInput input);

    @Mapping(target = "irradiation.unitNumber.value", source = "unitNumber")
    @Mapping(target = "irradiation.productCode.value", source = "productCode")
    @Mapping(target = "irradiation.shortDescription", source = "productDescription")
    @Mapping(target = "irradiation.expirationDate", expression = "java(createExpirationDate(productCreatedInput.expirationDate(), productCreatedInput.expirationTime()))")
    @Mapping(target = "irradiation.collectionDate", source = "collectionDate")
    @Mapping(target = "irradiation.weight", source = "weight")
    @Mapping(target = "irradiation.inventoryLocation", source = "inventoryLocation")
    @Mapping(target = "irradiation.collectionLocation", source = "collectionLocation")
    @Mapping(target = "irradiation.collectionTimeZone", source = "collectionTimeZone")
    @Mapping(target = "irradiation.productFamily", source = "productFamily")
    @Mapping(target = "irradiation.aboRh", source = "aboRh")
    @Mapping(target = "irradiation.id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "irradiation.inputProducts", source = "inputProducts")
    @Mapping(target = "irradiation.isLabeled", expression = "java(java.lang.Boolean.FALSE)")
    @Mapping(target = "irradiation.isLicensed", source = "licensed")
    @Mapping(target = "irradiation.inventoryStatus", expression = "java(InventoryStatus.AVAILABLE)")
    @Mapping(target = "irradiation.temperatureCategory", source = "temperatureCategory")
    @Mapping(target = "notificationMessages", ignore = true)
    @Mapping(target = "properties", ignore = true)
    public abstract InventoryAggregate toAggregate(ProductCreatedInput productCreatedInput);

    List<String> toDetails(List<String> details, String context) {
        return details.stream().map(d -> textConfigService.getText(context + "_DETAIL", d)).toList();
    }

    LocalDateTime createExpirationDate(String expDate, String expTime) {
        if (Objects.nonNull(expDate) && Objects.nonNull(expTime)) {
            return LocalDateTime.of(LocalDate.parse(expDate, DateTimeFormatter.ofPattern("MM/dd/yyyy")), LocalTime.parse(expTime));
        }
        return null;
    }

    @Mapping(target = "irradiation.unitNumber.value", source = "unitNumber")
    @Mapping(target = "irradiation.productCode.value", source = "productCode")
    @Mapping(target = "irradiation.shortDescription", source = "productDescription")
    @Mapping(target = "irradiation.collectionDate", source = "collectionDate")
    @Mapping(target = "irradiation.inventoryLocation", source = "inventoryLocation")
    @Mapping(target = "irradiation.collectionLocation", source = "collectionLocation")
    @Mapping(target = "irradiation.collectionTimeZone", source = "collectionTimeZone")
    @Mapping(target = "irradiation.productFamily", source = "productFamily")
    @Mapping(target = "irradiation.aboRh", source = "aboRh")
    @Mapping(target = "irradiation.id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "irradiation.isLabeled", expression = "java(java.lang.Boolean.FALSE)")
    @Mapping(target = "irradiation.inventoryStatus", expression = "java(InventoryStatus.AVAILABLE)")
    @Mapping(target = "notificationMessages", ignore = true)
    @Mapping(target = "properties", ignore = true)
    public abstract InventoryAggregate toAggregate(CheckInCompletedInput checkInCompletedInput);



    @Mapping(target = "irradiation",  expression = "java(toInventory(productModifiedInput, parent))")
    @Mapping(target = "notificationMessages", ignore = true)
    @Mapping(target = "properties", ignore = true)
    public abstract InventoryAggregate toAggregate(ProductModifiedInput productModifiedInput, Inventory parent);


    @Mapping(target = "unitNumber.value",  source = "productModifiedInput.unitNumber")
    @Mapping(target = "productCode.value", source = "productModifiedInput.productCode")
    @Mapping(target = "shortDescription", source = "productModifiedInput.shortDescription")
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "expirationDate", expression = "java(createExpirationDate(productModifiedInput.expirationDate(), productModifiedInput.expirationTime()))")
    @Mapping(target = "weight", source = "productModifiedInput.weight")
    @Mapping(target = "modificationLocation", source = "productModifiedInput.modificationLocation")
    @Mapping(target = "productFamily", source = "productModifiedInput.productFamily")
    @Mapping(target = "productModificationDate", source = "productModifiedInput.modificationDate")
    @Mapping(target = "expirationTimeZone", source = "productModifiedInput.modificationTimeZone")
    @Mapping(target = "volumes", expression = "java(buildVolume(productModifiedInput))")
    @Mapping(target = "isLabeled", expression = "java(java.lang.Boolean.FALSE)")
    @Mapping(target = "isLicensed", expression = "java(java.lang.Boolean.FALSE)")
    @Mapping(target = "inventoryStatus", expression = "java(InventoryStatus.AVAILABLE)")
    @Mapping(target = "inputProducts", expression = "java(buildInputProducts(parent))")
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "deviceStored", ignore = true)
    @Mapping(target = "storageLocation", ignore = true)
    @Mapping(target = "temperatureCategory", ignore = true)
    @Mapping(target = "cartonNumber", ignore = true)
    @Mapping(target = "statusReason", ignore = true)
    @Mapping(target = "histories", ignore = true)
    public abstract Inventory toInventory(ProductModifiedInput productModifiedInput, Inventory parent);



    List<InputProduct> buildInputProducts(Inventory inventory) {
        return List.of(new InputProduct(inventory.getUnitNumber().value(), inventory.getProductCode().value()));
    }

    List<Volume> buildVolume(ProductModifiedInput productModifiedInput) {
        return List.of(new Volume(BioProConstants.PRODUCT_VOLUME_TYPE, productModifiedInput.volume(), BioProConstants.PRODUCT_VOLUME_UNIT));
    }
}
