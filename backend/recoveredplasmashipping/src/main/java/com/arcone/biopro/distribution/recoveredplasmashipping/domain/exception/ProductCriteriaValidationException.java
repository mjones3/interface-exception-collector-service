package com.arcone.biopro.distribution.recoveredplasmashipping.domain.exception;

import lombok.Getter;

@Getter
public class ProductCriteriaValidationException extends RuntimeException {
    private String errorType;
    private String errorName;


    public ProductCriteriaValidationException(String message) {
        super(message);
    }

    public ProductCriteriaValidationException(String message, String errorType , String errorName) {
        super(message);
        this.errorType = errorType;
        this.errorName  = errorName;
    }

    public ProductCriteriaValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductCriteriaValidationException(Throwable cause) {
        super(cause);
    }

}
