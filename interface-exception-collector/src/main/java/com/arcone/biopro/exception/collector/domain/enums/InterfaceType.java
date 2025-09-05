package com.arcone.biopro.exception.collector.domain.enums;

/**
 * Enumeration representing the different types of interfaces in the BioPro
 * system.
 * Used to categorize exceptions based on their source interface service.
 */
public enum InterfaceType {
    ORDER,
    COLLECTION,
    DISTRIBUTION,
    RECRUITMENT,
    PARTNER_ORDER,
    MOCK_RSOCKET
}