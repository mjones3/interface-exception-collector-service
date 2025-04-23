package com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import lombok.Getter;

@Getter
public class ProductValidationException extends RuntimeException {
    private InventoryValidation inventoryValidation;
    private String errorType;


    public ProductValidationException(String message , String errorType) {
        super(message);
        this.errorType = errorType;
    }

    public ProductValidationException(String message,InventoryValidation inventoryValidation, String errorType) {
        super(message);
        this.inventoryValidation = inventoryValidation;
        this.errorType = errorType;
    }

    public ProductValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductValidationException(Throwable cause) {
        super(cause);
    }

}
