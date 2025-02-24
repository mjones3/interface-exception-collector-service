package com.arcone.biopro.distribution.shipping.domain.model.enumeration;

public enum ExternalTransferStatus {

    PENDING("Pending")
    ,COMPLETE("Complete");

    public final String label;

    ExternalTransferStatus(String label) {
        this.label = label;
    }
}
