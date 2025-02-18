package com.arcone.biopro.distribution.shipping.application.exception;

import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ShipmentValidationException extends RuntimeException {

    private List<NotificationDTO> notifications;
    private String nextUrl;
    private Map<String, List<?>> results;
    private String errorType;

    public ShipmentValidationException(String message) {
        super(message);
    }

    public ShipmentValidationException(String message ,List<NotificationDTO> notifications , String nextUrl, String errorType) {
        super(message);
        this.notifications = notifications;
        this.nextUrl = nextUrl;
        this.errorType = errorType;
    }

    public ShipmentValidationException(String message ,List<NotificationDTO> notifications , String nextUrl , Map<String, List<?>> results , String errorType) {
        super(message);
        this.notifications = notifications;
        this.nextUrl = nextUrl;
        this.results = results;
        this.errorType = errorType;
    }

    public ShipmentValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShipmentValidationException(Throwable cause) {
        super(cause);
    }
}
