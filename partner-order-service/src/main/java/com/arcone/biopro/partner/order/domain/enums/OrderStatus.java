package com.arcone.biopro.partner.order.domain.enums;

/**
 * Enumeration of possible order statuses in the Partner Order Service.
 */
public enum OrderStatus {
    RECEIVED,
    PROCESSING,
    VALIDATED,
    PUBLISHED,
    FAILED,
    REJECTED
}