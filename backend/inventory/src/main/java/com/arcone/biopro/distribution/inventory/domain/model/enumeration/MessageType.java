package com.arcone.biopro.distribution.inventory.domain.model.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum MessageType {
    INVENTORY_NOT_FOUND_IN_LOCATION(1, null,  NotificationType.WARN, Action.BACK_TO_STORAGE),
    INVENTORY_IS_EXPIRED(2, null, NotificationType.INFO, Action.TRIGGER_DISCARD),
    INVENTORY_IS_UNSUITABLE(3, InventoryConditions.UNSUITABLE.name(), NotificationType.INFO, Action.TRIGGER_DISCARD),
    INVENTORY_IS_QUARANTINED(4, InventoryConditions.QUARANTINED.name(), NotificationType.INFO, Action.BACK_TO_STORAGE),
    INVENTORY_IS_DISCARDED(5, InventoryStatus.DISCARDED.name(), NotificationType.INFO, Action.PLACE_IN_BIOHAZARD),
    INVENTORY_NOT_EXIST(6, null, NotificationType.WARN, Action.BACK_TO_STORAGE),
    INVENTORY_IS_SHIPPED(7, InventoryStatus.SHIPPED.name(), NotificationType.WARN, Action.BACK_TO_STORAGE),
    INVENTORY_IS_UNLABELED(8, null, NotificationType.INFO, Action.BACK_TO_STORAGE);

    Integer code;
    String status;
    NotificationType type;
    Action action;


    public static Optional<MessageType> fromStatus(InventoryStatus status) {
        return Arrays.stream(MessageType.values())
            .filter(item -> Objects.nonNull(item.status))
            .filter(nt -> nt.status.equals(status.name()))
            .findFirst();
    }
}
