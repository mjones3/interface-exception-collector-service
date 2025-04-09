package com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import lombok.Getter;

@Getter
public class ProductValidationException extends RuntimeException {
    private InventoryValidation inventoryValidation;


    public ProductValidationException(String message) {
        super(message);
    }

    public ProductValidationException(String message,InventoryValidation inventoryValidation) {
        super(message);
        this.inventoryValidation = inventoryValidation;
    }

    public ProductValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductValidationException(Throwable cause) {
        super(cause);
    }

}
