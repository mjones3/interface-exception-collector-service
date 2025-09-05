package com.arcone.biopro.partner.order.application.service;

import com.arcone.biopro.partner.order.api.dto.PartnerOrderRequest;
import com.arcone.biopro.partner.order.api.dto.PartnerOrderResponse;

import com.arcone.biopro.partner.order.domain.entity.PartnerOrder;
import com.arcone.biopro.partner.order.domain.entity.PartnerOrderItem;
import com.arcone.biopro.partner.order.domain.enums.OrderStatus;
import com.arcone.biopro.partner.order.domain.event.OrderReceivedEvent;
import com.arcone.biopro.partner.order.domain.event.OrderRejectedEvent;
import com.arcone.biopro.partner.order.infrastructure.kafka.EventMapper;
import com.arcone.biopro.partner.order.infrastructure.kafka.EventPublishingService;
import com.arcone.biopro.partner.order.infrastructure.repository.PartnerOrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service for handling partner order business logic.
 * Coordinates validation, storage, and event publishing for order submissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerOrderService {

    private final PartnerOrderRepository partnerOrderRepository;
    private final ObjectMapper objectMapper;
    private final EventPublishingService eventPublishingService;
    private final EventMapper eventMapper;

    /**
     * Processes a partner order submission.
     * Handles both new orders and retry requests from Interface Exception
     * Collector.
     */
    @Transactional
    public PartnerOrderResponse processOrder(PartnerOrderRequest request, boolean isRetry, UUID originalTransactionId) {
        UUID correlationId = UUID.randomUUID();
        // Always generate a new UUID for transaction_id to avoid unique constraint violations
        // The originalTransactionId is kept for correlation purposes only
        UUID transactionId = UUID.randomUUID();

        // Set up MDC for logging
        MDC.put("correlationId", correlationId.toString());
        MDC.put("transactionId", transactionId.toString());
        MDC.put("externalId", request.getExternalId());

        try {
            if (isRetry && originalTransactionId != null) {
                log.info("Processing partner order retry - externalId: {}, originalTransactionId: {}, newTransactionId: {}",
                        request.getExternalId(), originalTransactionId, transactionId);
            } else {
                log.info("Processing partner order - externalId: {}, isRetry: {}",
                        request.getExternalId(), isRetry);
            }

            // Convert request to JSON for storage
            JsonNode originalPayload = objectMapper.valueToTree(request);

            // Create new partner order entity (always create new, even for retries)
            // Duplicate external_ids are now allowed for retry scenarios
            PartnerOrder partnerOrder = PartnerOrder.builder()
                    .transactionId(transactionId)
                    .externalId(request.getExternalId())
                    .status(OrderStatus.RECEIVED)
                    .originalPayload(originalPayload)
                    .locationCode(request.getLocationCode())
                    .productCategory(request.getProductCategory())
                    .submittedAt(OffsetDateTime.now())
                    .build();

            // Add order items
            if (request.getOrderItems() != null) {
                for (PartnerOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
                    PartnerOrderItem item = PartnerOrderItem.builder()
                            .productFamily(itemRequest.getProductFamily())
                            .bloodType(itemRequest.getBloodType())
                            .quantity(itemRequest.getQuantity())
                            .comments(itemRequest.getComments())
                            .build();
                    partnerOrder.addOrderItem(item);
                }
            }

            // Save to database
            PartnerOrder savedOrder = partnerOrderRepository.save(partnerOrder);
            log.info("Partner order saved successfully - id: {}, transactionId: {}",
                    savedOrder.getId(), savedOrder.getTransactionId());

            // Update status to validated
            savedOrder.updateStatus(OrderStatus.VALIDATED);
            partnerOrderRepository.save(savedOrder);

            // Publish events to Kafka
            publishOrderEvents(savedOrder, correlationId);

            // Mark as processed
            savedOrder.markAsProcessed();
            savedOrder.updateStatus(OrderStatus.PUBLISHED);
            partnerOrderRepository.save(savedOrder);

            log.info("Partner order processed successfully - transactionId: {}", transactionId);

            return PartnerOrderResponse.success(
                    transactionId,
                    request.getExternalId(),
                    request.getLocationCode(),
                    correlationId);

        } catch (Exception e) {
            log.error("Error processing partner order", e);
            return PartnerOrderResponse.error(
                    "PROCESSING_FAILED",
                    "Failed to process order: " + e.getMessage(),
                    request.getExternalId(),
                    correlationId);
        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }

    /**
     * Processes a new partner order submission.
     */
    public PartnerOrderResponse processOrder(PartnerOrderRequest request) {
        return processOrder(request, false, null);
    }

    /**
     * Processes a retry request from Interface Exception Collector.
     */
    public PartnerOrderResponse processRetry(PartnerOrderRequest request, UUID originalTransactionId) {
        return processOrder(request, true, originalTransactionId);
    }

    /**
     * Publishes Kafka events for a processed order.
     * Publishes both OrderReceived and OrderRejected events for testing purposes.
     *
     * @param partnerOrder  the processed partner order
     * @param correlationId correlation ID for event tracing
     */
    private void publishOrderEvents(PartnerOrder partnerOrder, UUID correlationId) {
        try {
            log.info("Publishing Kafka events for order - transactionId: {}, externalId: {}",
                    partnerOrder.getTransactionId(), partnerOrder.getExternalId());

            // Create and publish OrderReceived event
            OrderReceivedEvent orderReceivedEvent = eventMapper.toOrderReceivedEvent(partnerOrder, correlationId);
            eventPublishingService.publishOrderReceived(orderReceivedEvent);

            // Create and publish OrderRejected event for testing Interface Exception
            // Collector
            OrderRejectedEvent orderRejectedEvent = eventMapper.createTestOrderRejectedEvent(partnerOrder,
                    correlationId);
            eventPublishingService.publishOrderRejected(orderRejectedEvent);

            log.info("Successfully initiated publishing of Kafka events for order - transactionId: {}",
                    partnerOrder.getTransactionId());

        } catch (Exception e) {
            log.error("Error publishing Kafka events for order - transactionId: {}, error: {}",
                    partnerOrder.getTransactionId(), e.getMessage(), e);
            // Don't fail the order processing if event publishing fails
            // The order is still valid and stored in the database
        }
    }
}