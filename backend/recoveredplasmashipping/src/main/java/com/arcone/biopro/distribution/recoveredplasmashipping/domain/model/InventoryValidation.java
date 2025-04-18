package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class InventoryValidation {
    private Inventory inventory;
    private List<InventoryNotification> notifications;

    public InventoryValidation(Inventory inventory, List<InventoryNotification> notifications) {
        this.inventory = inventory;
        this.notifications = notifications;
    }

    public InventoryNotification getFistNotification(){
        if(notifications == null || notifications.isEmpty()){
            return null;
        }

        return notifications.getFirst();
    }
}
