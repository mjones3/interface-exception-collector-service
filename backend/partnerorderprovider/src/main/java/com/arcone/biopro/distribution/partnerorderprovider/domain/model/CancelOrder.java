package com.arcone.biopro.distribution.partnerorderprovider.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class CancelOrder {

    private UUID id;
    private String externalId;
    private String cancelDate;
    private String cancelEmployeeCode;
    private String cancelReason;

    public CancelOrder(String externalId, String cancelDate, String cancelEmployeeCode, String cancelReason) {
        this.id = UUID.randomUUID();
        this.externalId = Objects.requireNonNull(externalId,"External ID cannot be null");
        this.cancelDate = Objects.requireNonNull(externalId,"Cancel Date cannot be null");
        this.cancelReason = Objects.requireNonNull(externalId,"Cancel Reason cannot be null");
        this.cancelEmployeeCode = cancelEmployeeCode;

    }
}
