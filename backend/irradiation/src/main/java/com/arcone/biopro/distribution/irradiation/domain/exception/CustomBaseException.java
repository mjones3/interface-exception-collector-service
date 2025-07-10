package com.arcone.biopro.distribution.irradiation.domain.exception;

import graphql.ErrorClassification;
import lombok.Getter;
import org.springframework.graphql.execution.ErrorType;

@Getter
public class CustomBaseException extends RuntimeException {

    protected static String ERROR_TYPE = "error";
    protected String type = ERROR_TYPE;

    public CustomBaseException(Throwable cause) {
        super(cause);
    }

    public ErrorClassification getClassification() {
        return ErrorType.INTERNAL_ERROR;
    }

}
