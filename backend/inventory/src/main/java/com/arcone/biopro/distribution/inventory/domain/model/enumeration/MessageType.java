package com.arcone.biopro.distribution.inventory.domain.model.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum MessageType {
    INVENTORY_NOT_FOUND_IN_LOCATION(1, null,  NotificationType.WARN),
    INVENTORY_IS_EXPIRED(2, null, NotificationType.INFO),
    INVENTORY_IS_UNSUITABLE(3, InventoryStatus.UNSUITABLE, NotificationType.INFO),
    INVENTORY_IS_QUARANTINED(4, InventoryStatus.QUARANTINED, NotificationType.INFO),
    INVENTORY_IS_DISCARDED(5, InventoryStatus.DISCARDED, NotificationType.INFO),
    INVENTORY_NOT_EXIST(6, null, NotificationType.WARN),
    INVENTORY_IS_SHIPPED(7, InventoryStatus.SHIPPED, NotificationType.WARN);

    Integer code;
    InventoryStatus status;
    NotificationType type;

    public static Optional<MessageType> fromStatus(InventoryStatus status) {
        return Arrays.stream(MessageType.values())
            .filter(nt -> nt.status == status)
            .findFirst();
    }
}
