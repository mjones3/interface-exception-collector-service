package com.arcone.biopro.partner.order.application.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested payload cannot be found.
 */
public class PayloadNotFoundException extends RuntimeException {

    private final UUID transactionId;

    public PayloadNotFoundException(UUID transactionId) {
        super("Payload not found for transaction ID: " + transactionId);
        this.transactionId = transactionId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }
}