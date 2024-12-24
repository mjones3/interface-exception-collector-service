package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.exception.UnavailableStatusNotMappedException;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.NotificationMessage;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.arcone.biopro.distribution.inventory.BioProConstants.EXPIRED;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAggregate {

    public static final String OTHER_REASON = "OTHER";
    Inventory inventory;

    List<NotificationMessage> notificationMessages;

    public Boolean isExpired() {
        return inventory.getExpirationDate().isBefore(LocalDateTime.now());
    }

    public InventoryAggregate checkIfIsValidToShip(String location) {
        notificationMessages = new ArrayList<>();

        if (!inventory.getLocation().equals(location)) {
            notificationMessages.add(createNotificationMessage(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION, null));
        }
        else if (isQuarantined()) {
            notificationMessages.addAll(createQuarantinesNotificationMessage());
        }
        else if (!inventory.getInventoryStatus().equals(InventoryStatus.AVAILABLE)) {
            notificationMessages.addAll(createNotificationMessage());
        }
        else if (!inventory.getIsLabeled()) {
            notificationMessages.add(createNotificationMessage(MessageType.INVENTORY_IS_UNLABELED, null));
        }
        else if (isExpired()) {
            notificationMessages.add(createNotificationMessage(MessageType.INVENTORY_IS_EXPIRED, EXPIRED));
        }
        return this;
    }

    private NotificationMessage createNotificationMessage(MessageType notificationType, String reason) {
        return new NotificationMessage(notificationType.name(), notificationType.getCode(), notificationType.name(), notificationType.getType().name(), notificationType.getAction().name(), reason, List.of());
    }

    public Boolean isQuarantined() {
        return !inventory.getQuarantines().isEmpty();
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

    public InventoryAggregate completeShipment() {
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
        return this;
    }

    public InventoryAggregate addQuarantine(Long quarantineId, String reason, String comments) {
        inventory.addQuarantine(quarantineId, reason, comments);

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

    public InventoryAggregate label(Boolean isLicensed, String finalProductCode) {
        inventory.setIsLabeled(true);
        inventory.setIsLicensed(isLicensed);
        inventory.setProductCode(new ProductCode(finalProductCode));
        return this;
    }
}
