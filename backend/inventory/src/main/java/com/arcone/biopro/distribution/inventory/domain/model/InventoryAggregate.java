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
            notificationMessages.add(createNotificationMessage(MessageType.INVENTORY_IS_EXPIRED));
        }

        if (!inventory.getLocation().equals(location)) {
            notificationMessages.add(createNotificationMessage(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION));
        }

        return this;
    }

    private NotificationMessage createNotificationMessage(MessageType notificationType) {
        return new NotificationMessage(notificationType.name(), notificationType.getCode(), notificationType.name(), notificationType.getType().name(), notificationType.getAction().name());
    }

    private List<NotificationMessage> createNotificationMessage() {

        if(inventory.getInventoryStatus().equals(InventoryStatus.QUARANTINED)) {
            return createQuarantinesNotificationMessage();
        }

        MessageType messageType = MessageType.fromStatus(inventory.getInventoryStatus())
            .orElseThrow(UnavailableStatusNotMappedException::new);

        return List.of(new NotificationMessage(messageType.name(), messageType.getCode(), Strings.isNotBlank(inventory.getStatusReason()) ? inventory.getStatusReason() : messageType.name(), messageType.getType().name(), messageType.getAction().name()));
    }

    private List<NotificationMessage> createQuarantinesNotificationMessage() {
        MessageType qt = MessageType.INVENTORY_IS_QUARANTINED;
        return inventory.getQuarantines().stream()
            .map(q -> new NotificationMessage(
                qt.name(),
                qt.getCode(),
                !q.reason().equals(OTHER_SEE_COMMENTS) ? q.reason() : String.format("%s: %s", OTHER_SEE_COMMENTS, q.comment()),
                qt.getType().name(),
                qt.getAction().name()))
            .toList();
    }

    public InventoryAggregate completeShipment() {
        inventory.setInventoryStatus(InventoryStatus.SHIPPED);
        return this;
    }

    public InventoryAggregate updateStorage(String deviceStored, String storageLocation) {
        inventory.setDeviceStored(deviceStored);
        inventory.setStorageLocation(storageLocation);
        return this;
    }

    public InventoryAggregate discardProduct(String reason, String comments) {
        inventory.setStatusReason(reason);
        inventory.setComments(comments);
        inventory.setInventoryStatus(InventoryStatus.DISCARDED);
        return this;
    }
}
