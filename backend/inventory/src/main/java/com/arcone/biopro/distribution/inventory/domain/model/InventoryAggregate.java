package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.exception.UnavailableStatusNotMappedException;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.NotificationMessage;
import lombok.Builder;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.arcone.biopro.distribution.inventory.BioProConstants.EXPIRED;

@Builder
@Getter
public class InventoryAggregate {

    public static final String OTHER_SEE_COMMENTS = "OTHER_SEE_COMMENTS";
    Inventory inventory;

    List<NotificationMessage> notificationMessages;

    public Boolean isExpired() {
        return inventory.getExpirationDate().isBefore(LocalDateTime.now());
    }

    public InventoryAggregate checkIfIsValidToShip(String location) {
        notificationMessages = new ArrayList<>();

        if (!inventory.getInventoryStatus().equals(InventoryStatus.AVAILABLE)) {
            notificationMessages.addAll(createNotificationMessage());
        }

        if (isExpired()) {
            notificationMessages.add(createNotificationMessage(MessageType.INVENTORY_IS_EXPIRED, EXPIRED));
        }

        if (!inventory.getLocation().equals(location)) {
            notificationMessages.add(createNotificationMessage(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION, null));
        }

        return this;
    }

    private NotificationMessage createNotificationMessage(MessageType notificationType, String reason) {
        return new NotificationMessage(notificationType.name(), notificationType.getCode(), notificationType.name(), notificationType.getType().name(), notificationType.getAction().name(), reason);
    }

    private List<NotificationMessage> createNotificationMessage() {

        if (inventory.getInventoryStatus().equals(InventoryStatus.QUARANTINED)) {
            return createQuarantinesNotificationMessage();
        }

        MessageType messageType = MessageType.fromStatus(inventory.getInventoryStatus())
            .orElseThrow(UnavailableStatusNotMappedException::new);

        return List.of(new NotificationMessage(
            messageType.name(),
            messageType.getCode(),
            Strings.isNotBlank(inventory.getStatusReason()) ? inventory.getStatusReason() : messageType.name(),
            messageType.getType().name(), messageType.getAction().name(),
            null));
    }

    private List<NotificationMessage> createQuarantinesNotificationMessage() {
        MessageType qt = MessageType.INVENTORY_IS_QUARANTINED;
        return inventory.getQuarantines().stream()
            .map(q -> new NotificationMessage(
                qt.name(),
                qt.getCode(),
                !q.reason().equals(OTHER_SEE_COMMENTS) ? q.reason() : String.format("%s: %s", OTHER_SEE_COMMENTS, q.comments()),
                qt.getType().name(),
                qt.getAction().name(),
                null))
            .toList();
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
}
