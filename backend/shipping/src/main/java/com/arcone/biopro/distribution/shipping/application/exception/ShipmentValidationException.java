package com.arcone.biopro.distribution.shipping.application.exception;

import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class ShipmentValidationException extends RuntimeException {

    private List<NotificationDTO> notifications;
    private String nextUrl;

    public ShipmentValidationException(String message) {
        super(message);
    }

    public ShipmentValidationException(String message ,List<NotificationDTO> notifications , String nextUrl) {
        super(message);
        this.notifications = notifications;
        this.nextUrl = nextUrl;
    }

    public ShipmentValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShipmentValidationException(Throwable cause) {
        super(cause);
    }
}
