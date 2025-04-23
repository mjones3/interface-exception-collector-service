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
public class InventoryNotification implements Validatable {
    private String errorName;
    private Integer errorCode;
    private String errorMessage;
    private String errorType;
    private String action;
    private String reason;
    private List<String> details;

    public InventoryNotification(String errorName, Integer errorCode, String errorMessage, String errorType, String action, String reason, List<String> details) {
        this.errorName = errorName;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
        this.action = action;
        this.reason = reason;
        this.details = details;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.errorName == null || this.errorName.isBlank()) {
            throw new IllegalArgumentException("Error Name is required");
        }

        if (this.errorCode == null) {
            throw new IllegalArgumentException("Error Code is required");
        }

        if (this.errorMessage == null || this.errorMessage.isBlank()) {
            throw new IllegalArgumentException("Error Message is required");
        }

        if (this.errorType == null || this.errorType.isBlank()) {
            throw new IllegalArgumentException("Error Type is required");
        }

    }
}
