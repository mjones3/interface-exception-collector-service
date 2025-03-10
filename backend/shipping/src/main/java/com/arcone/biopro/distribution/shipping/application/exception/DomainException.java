package com.arcone.biopro.distribution.shipping.application.exception;

import com.arcone.biopro.distribution.shipping.application.dto.UseCaseMessageType;
import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final UseCaseMessageType useCaseMessageType;

    public DomainException(UseCaseMessageType useCaseMessageType) {
        super();
        this.useCaseMessageType = useCaseMessageType;
    }

    public DomainException(UseCaseMessageType useCaseMessageType, String message) {
        super(message);
        this.useCaseMessageType = useCaseMessageType;
    }

    public DomainException(UseCaseMessageType useCaseMessageType, String message, Throwable cause) {
        super(message, cause);
        this.useCaseMessageType = useCaseMessageType;
    }

    public DomainException(UseCaseMessageType useCaseMessageType, Throwable cause) {
        super(cause);
        this.useCaseMessageType = useCaseMessageType;
    }

}
