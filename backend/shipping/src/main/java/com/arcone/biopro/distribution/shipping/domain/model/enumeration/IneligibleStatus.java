package com.arcone.biopro.distribution.shipping.domain.model.enumeration;

public enum IneligibleStatus {
    INVENTORY_NOT_FOUND_IN_LOCATION("Inventory Not Found")
    , INVENTORY_IS_EXPIRED("Expired")
    , INVENTORY_IS_UNSUITABLE("Unsuitable")
    ,  INVENTORY_IS_QUARANTINED("Quarantined")
    ,  INVENTORY_IS_DISCARDED("Discarded")
    , INVENTORY_NOT_EXIST("Inventory Not Exist")
    , INVENTORY_IS_SHIPPED("Already Shipped");

    public final String label;

    IneligibleStatus(String label) {
        this.label = label;
    }
}
