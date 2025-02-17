package com.arcone.biopro.distribution.shipping.domain.model.enumeration;

public enum ProductLocationHistoryType {
    SHIPPING("Shipping")
    ,EXTERNAL_TRANSFER("External Transfer");

    public final String label;

    ProductLocationHistoryType(String label) {
        this.label = label;
    }

}
