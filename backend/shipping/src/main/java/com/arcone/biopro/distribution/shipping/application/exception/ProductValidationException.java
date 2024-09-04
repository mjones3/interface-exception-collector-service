package com.arcone.biopro.distribution.shipping.application.exception;

import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductValidationException extends RuntimeException {
    private List<NotificationDTO> notifications;

    public ProductValidationException(String message) {
        super(message);
    }

    public ProductValidationException(String message,List<NotificationDTO> notifications) {
        super(message);
        this.notifications = notifications;
    }

    public ProductValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductValidationException(Throwable cause) {
        super(cause);
    }

}
