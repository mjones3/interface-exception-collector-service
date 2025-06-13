package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.exception.UnavailableStatusNotMappedException;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.*;
import com.arcone.biopro.distribution.inventory.domain.model.vo.NotificationMessage;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.arcone.biopro.distribution.inventory.BioProConstants.EXPIRED;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class InventoryAggregate {

    public static final String OTHER_REASON = "OTHER";
    Inventory inventory;

    @Builder.Default
    List<Property> properties = new ArrayList<>();

    List<NotificationMessage> notificationMessages;

    public Boolean isExpired() {
        return inventory.isExpired();
    }

    public InventoryAggregate checkIfIsValidToShip(String location) {
        notificationMessages = new ArrayList<>();

        if (!inventory.getInventoryLocation().equals(location)) {
            notificationMessages.add(createNotificationMessage(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION, null));
        }
        else if (inventory.getInventoryStatus().equals(InventoryStatus.DISCARDED)) {
            notificationMessages.addAll(createNotificationMessage());
        }
        else if (isUnsuitable()) {
            notificationMessages.addAll(createUnsuitableNotificationMessage());
        }
        else if (isQuarantined()) {
            notificationMessages.addAll(createQuarantinesNotificationMessage());
        }
        else if (!inventory.getIsLabeled()) {
            notificationMessages.add(createNotificationMessage(MessageType.INVENTORY_IS_UNLABELED, null));
        }
        else if (isExpired()) {
            notificationMessages.add(createNotificationMessage(MessageType.INVENTORY_IS_EXPIRED, EXPIRED));
        }
        else if (!inventory.getInventoryStatus().equals(InventoryStatus.AVAILABLE)) {
            notificationMessages.addAll(createNotificationMessage());
        }

        return this;
    }

    private Collection<NotificationMessage> createUnsuitableNotificationMessage() {
        MessageType messageType = MessageType.INVENTORY_IS_UNSUITABLE;

        return List.of(new NotificationMessage(
            messageType.name(),
            messageType.getCode(),
            inventory.getUnsuitableReason(),
            messageType.getType().name(), messageType.getAction().name(),
            inventory.getUnsuitableReason(),
            List.of()));
    }

    private boolean isUnsuitable() {
        return Objects.nonNull(inventory.getUnsuitableReason());
    }

    private NotificationMessage createNotificationMessage(MessageType notificationType, String reason) {
        return new NotificationMessage(notificationType.name(), notificationType.getCode(), notificationType.name(), notificationType.getType().name(), notificationType.getAction().name(), reason, List.of());
    }

    public Boolean isQuarantined() {
        return hasPropertyEquals(PropertyKey.QUARANTINED, "Y") || inventory.isQuarantined();
    }

    private List<NotificationMessage> createNotificationMessage() {

        MessageType messageType = MessageType.fromStatus(inventory.getInventoryStatus())
            .orElseThrow(UnavailableStatusNotMappedException::new);

        return List.of(new NotificationMessage(
            messageType.name(),
            messageType.getCode(),
            buildMessage(messageType),
            messageType.getType().name(), messageType.getAction().name(),
            null,
            List.of()));
    }

    private String buildMessage(MessageType messageType) {
        return Strings.isNotBlank(inventory.getStatusReason())
            ? (
                inventory.getStatusReason().equals(OTHER_REASON)
                ? String.format("%s: %s", OTHER_REASON, inventory.getComments())
                : inventory.getStatusReason()
            )
            : messageType.name();
    }

    private List<NotificationMessage> createQuarantinesNotificationMessage() {
        MessageType qt = MessageType.INVENTORY_IS_QUARANTINED;



        List<String> details = inventory.getQuarantines().stream().map(q -> !q.reason().equals(OTHER_REASON) ? q.reason() : String.format("%s: %s", OTHER_REASON, q.comments())).toList();

        return List.of(new NotificationMessage(
            qt.name(),
            qt.getCode(),
            qt.name(),
            qt.getType().name(),
            qt.getAction().name(),
            null,
            details));
    }

    public InventoryAggregate completeShipment(ShipmentType shipmentType) {
        if(ShipmentType.INTERNAL_TRANSFER.equals(shipmentType)) {
            transitionStatus(InventoryStatus.IN_TRANSIT, null);
            return this;
        }
        transitionStatus(InventoryStatus.SHIPPED, null);
        return this;
    }

    public InventoryAggregate updateStorage(String deviceStored, String storageLocation) {
        inventory.setDeviceStored(deviceStored);
        inventory.setStorageLocation(storageLocation);
        return this;
    }

    public InventoryAggregate discardProduct(String reason, String comments) {
        transitionStatus(InventoryStatus.DISCARDED, reason);

        inventory.setComments(comments);

        return this;
    }

    public InventoryAggregate removeQuarantine(Long quarantineId) {
        inventory.removeQuarantine(quarantineId);

        if (inventory.getQuarantines().isEmpty()) {
            removeProperty(PropertyKey.QUARANTINED);
        }
        return this;
    }

    public InventoryAggregate addQuarantine(Long quarantineId, String reason, String comments) {
        inventory.addQuarantine(quarantineId, reason, comments);
        addQuarantineFlag();

        return this;
    }

    public InventoryAggregate recoveryStatus() {
        inventory.restoreHistory();
        return this;
    }

    public InventoryAggregate updateQuarantine(Long quarantineId, String reason, String comments) {
        inventory.updateQuarantine(quarantineId, reason, comments);

        return this;
    }

    private void transitionStatus(InventoryStatus newStatus, String statusReason) {
        inventory.transitionStatus(newStatus, statusReason);
    }

    public boolean isAvailable() {
        return InventoryStatus.AVAILABLE.equals(this.inventory.getInventoryStatus());
    }

    public boolean getIsLabeled() {
        return this.inventory.getIsLabeled();
    }

    public InventoryAggregate convertProduct() {
        inventory.transitionStatus(InventoryStatus.CONVERTED, "Child manufactured");
        return this;
    }

    public boolean hasParent() {
        return !this.inventory.getInputProducts().isEmpty();
    }

    public InventoryAggregate label(Boolean isLicensed, String finalProductCode, LocalDateTime expirationDate) {
        inventory.setIsLabeled(true);
        inventory.setIsLicensed(isLicensed);
        inventory.setProductCode(new ProductCode(finalProductCode));
        inventory.setExpirationDate(expirationDate);
        return this;
    }

    public InventoryAggregate invalidLabel() {
        inventory.setIsLabeled(false);
        inventory.setIsLicensed(false);
        return this;
    }

    public InventoryAggregate unsuit(String reason) {
        if(inventory.isConverted()) {
            log.info("Skipping unsuitable for converted products");
            return this;
        }
        inventory.setUnsuitableReason(reason);
        return this;
    }

    public InventoryAggregate updateTemperatureCategory(String temperatureCategory) {
        inventory.setTemperatureCategory(temperatureCategory);
        return this;
    }

    public InventoryAggregate completeProduct(List<Volume> volumes, AboRhType aboRh) {
       if (Objects.nonNull(volumes) && !volumes.isEmpty()) {
           volumes.forEach(item -> inventory.addVolume(item.getType(), item.getValue(), item.getUnit()));
       }
        if(Objects.nonNull(aboRh)) {
            inventory.setAboRh(aboRh);
        }
       return this;
    }

    public InventoryAggregate putInTheCarton(String cartonNumber) {
        inventory.transitionStatus(InventoryStatus.PACKED, null);
        inventory.setCartonNumber(cartonNumber);
        return this;
    }

    public InventoryAggregate removeFromCarton(String cartonNumber) {
        if (cartonNumber.equals(inventory.getCartonNumber())) {
            inventory.transitionStatus(InventoryStatus.AVAILABLE, null);
            inventory.setCartonNumber(null);
        }
        return this;
    }

    public InventoryAggregate cartonShipped() {
        inventory.transitionStatus(InventoryStatus.SHIPPED, null);
        return this;
    }

    public InventoryAggregate modifyProduct() {
        inventory.transitionStatus(InventoryStatus.MODIFIED, "Product modified");
        return this;
    }

    public void addQuarantineFlag() {
        addProperty(PropertyKey.QUARANTINED, "Y");
    }

    public void addImportedFlag() {
        addProperty(PropertyKey.IMPORTED, "Y");
        inventory.setIsLabeled(true);
    }

    private void addProperty(PropertyKey key, String value) {
        Property property = new Property(key.name(), value);
        properties.removeIf(p -> p.equals(property));
        properties.add(property);
    }

    private void removeProperty(PropertyKey key) {
        Property property = new Property(key.name(), null);
        properties.removeIf(p -> p.equals(property));
    }

    private Boolean hasPropertyEquals(PropertyKey key, String value) {
        return properties.stream().anyMatch(p -> p.getKey().equals(key.name()) && p.getValue().equals(value));
    }

    public InventoryAggregate populateProperties(List<Property> properties) {
        this.properties = properties;
        return this;
    }
}

