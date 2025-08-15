package com.arcone.biopro.partner.order.application.service;

import com.arcone.biopro.partner.order.api.dto.PayloadResponse;
import com.arcone.biopro.partner.order.application.exception.PayloadNotFoundException;
import com.arcone.biopro.partner.order.domain.entity.PartnerOrder;
import com.arcone.biopro.partner.order.infrastructure.repository.PartnerOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for retrieving original order payloads.
 * Used by the Interface Exception Collector Service for retry operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayloadRetrievalService {

    private final PartnerOrderRepository partnerOrderRepository;

    /**
     * Retrieves the original payload for a given transaction ID.
     * Used by Interface Exception Collector for retry operations.
     */
    @Transactional(readOnly = true)
    public PayloadResponse getOriginalPayload(UUID transactionId) {
        // Set up MDC for logging
        MDC.put("transactionId", transactionId.toString());

        try {
            log.info("Retrieving original payload for transaction: {}", transactionId);

            PartnerOrder partnerOrder = partnerOrderRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> {
                        log.warn("Payload not found for transaction: {}", transactionId);
                        return new PayloadNotFoundException(transactionId);
                    });

            log.info("Original payload retrieved successfully - externalId: {}, status: {}",
                    partnerOrder.getExternalId(), partnerOrder.getStatus());

            // Set external ID in MDC for logging
            MDC.put("externalId", partnerOrder.getExternalId());

            return PayloadResponse.fromOrderData(
                    partnerOrder.getTransactionId(),
                    partnerOrder.getOriginalPayload(),
                    partnerOrder.getSubmittedAt(),
                    partnerOrder.getExternalId(),
                    partnerOrder.getLocationCode(),
                    partnerOrder.getStatus().name(),
                    partnerOrder.getProcessedAt());

        } catch (PayloadNotFoundException e) {
            log.error("Payload not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving payload for transaction: {}", transactionId, e);
            throw new RuntimeException("Failed to retrieve payload: " + e.getMessage(), e);
        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }

    /**
     * Checks if a payload exists for the given transaction ID.
     */
    @Transactional(readOnly = true)
    public boolean payloadExists(UUID transactionId) {
        try {
            return partnerOrderRepository.findByTransactionId(transactionId).isPresent();
        } catch (Exception e) {
            log.error("Error checking payload existence for transaction: {}", transactionId, e);
            return false;
        }
    }

    /**
     * Gets basic order information without the full payload.
     * Useful for lightweight checks.
     */
    @Transactional(readOnly = true)
    public PartnerOrder getOrderInfo(UUID transactionId) {
        return partnerOrderRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PayloadNotFoundException(transactionId));
    }
}