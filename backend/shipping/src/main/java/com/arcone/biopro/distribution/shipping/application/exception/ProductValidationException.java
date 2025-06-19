package com.arcone.biopro.distribution.shipping.application.exception;

import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductValidationException extends RuntimeException {
    private List<NotificationDTO> notifications;
    private InventoryResponseDTO inventoryResponseDTO;

    public ProductValidationException(String message) {
        super(message);
    }

    public ProductValidationException(String message,List<NotificationDTO> notifications) {
        super(message);
        this.notifications = notifications;
    }

    public ProductValidationException(String message, InventoryResponseDTO inventoryResponseDTO,List<NotificationDTO> notifications) {
        super(message);
        this.notifications = notifications;
        this.inventoryResponseDTO = inventoryResponseDTO;
    }

    public ProductValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductValidationException(Throwable cause) {
        super(cause);
    }

}
