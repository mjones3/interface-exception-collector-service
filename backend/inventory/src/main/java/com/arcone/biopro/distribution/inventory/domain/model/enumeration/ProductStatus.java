package com.arcone.biopro.distribution.inventory.domain.model.enumeration;

import lombok.Getter;

@Getter
public enum ProductStatus {

    READY_TO_BE_LABELED("Ready to be labeled"),
    NOT_READY_TO_LABEL("Not ready to label"),
    PRINTED("Printed"),
    LABELED("Labeled"),
    UNSUITABLE("Unsuitable"),
    DISCARDED("Discarded"),
    QUARANTINED("Quarantined");

    String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public static ProductStatus fromDescription(String description) {
        for (ProductStatus status : ProductStatus.values()) {
            if (status.getDescription().equalsIgnoreCase(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No ProductStatus with description " + description + " found.");
    }
}
