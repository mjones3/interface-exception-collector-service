package com.arcone.biopro.partner.order.infrastructure.kafka;

import com.arcone.biopro.partner.order.api.dto.PartnerOrderRequest;
import com.arcone.biopro.partner.order.domain.entity.PartnerOrder;
import com.arcone.biopro.partner.order.domain.entity.PartnerOrderItem;
import com.arcone.biopro.partner.order.domain.event.InvalidOrderEvent;
import com.arcone.biopro.partner.order.domain.event.OrderReceivedEvent;
import com.arcone.biopro.partner.order.domain.event.OrderRejectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper component for converting domain entities to Kafka event DTOs.
 * Handles the transformation of PartnerOrder entities and validation errors
 * into properly formatted event messages for Kafka publishing.
 */
@Component
@Slf4j
public class EventMapper {

        private static final String EVENT_VERSION = "1.0";
        private static final String EVENT_SOURCE = "partner-order-service";

        /**
         * Maps a PartnerOrder entity to an OrderReceived event.
         * This event indicates successful order processing and validation.
         *
         * @param partnerOrder  the partner order entity
         * @param correlationId correlation ID for event tracing
         * @return OrderReceived event ready for Kafka publishing
         */
        public OrderReceivedEvent toOrderReceivedEvent(PartnerOrder partnerOrder, UUID correlationId) {
                log.debug("Mapping PartnerOrder to OrderReceivedEvent - transactionId: {}, externalId: {}",
                                partnerOrder.getTransactionId(), partnerOrder.getExternalId());

                OrderReceivedEvent.OrderReceivedPayload payload = OrderReceivedEvent.OrderReceivedPayload.builder()
                                .externalId(partnerOrder.getExternalId())
                                .locationCode(partnerOrder.getLocationCode())
                                .productCategory(partnerOrder.getProductCategory())
                                .orderData(partnerOrder.getOriginalPayload())
                                .build();

                return OrderReceivedEvent.builder()
                                .eventId(UUID.randomUUID())
                                .eventType("OrderReceivedEvent")
                                .eventVersion(EVENT_VERSION)
                                .occurredOn(OffsetDateTime.now())
                                .source(EVENT_SOURCE)
                                .correlationId(correlationId)
                                .transactionId(partnerOrder.getTransactionId())
                                .payload(payload)
                                .build();
        }

        /**
         * Maps a PartnerOrder entity to an OrderRejected event for testing purposes.
         * This event is used to test the Interface Exception Collector functionality.
         *
         * @param partnerOrder   the partner order entity
         * @param correlationId  correlation ID for event tracing
         * @param rejectedReason reason for the rejection (for testing)
         * @return OrderRejected event ready for Kafka publishing
         */
        public OrderRejectedEvent toOrderRejectedEvent(PartnerOrder partnerOrder, UUID correlationId,
                        String rejectedReason) {
                log.debug("Mapping PartnerOrder to OrderRejectedEvent - transactionId: {}, externalId: {}, reason: {}",
                                partnerOrder.getTransactionId(), partnerOrder.getExternalId(), rejectedReason);

                // Map order items
                List<OrderRejectedEvent.OrderItem> orderItems = partnerOrder.getOrderItems().stream()
                                .map(this::mapToOrderRejectedItem)
                                .collect(Collectors.toList());

                OrderRejectedEvent.OrderRejectedPayload payload = OrderRejectedEvent.OrderRejectedPayload.builder()
                                .transactionId(partnerOrder.getTransactionId().toString())
                                .externalId(partnerOrder.getExternalId())
                                .rejectedReason(rejectedReason)
                                .operation("CREATE_ORDER")
                                .customerId(extractCustomerId(partnerOrder))
                                .locationCode(partnerOrder.getLocationCode())
                                .orderItems(orderItems)
                                .originalPayload(partnerOrder.getOriginalPayload())
                                .build();

                return OrderRejectedEvent.builder()
                                .eventId(UUID.randomUUID())
                                .eventType("OrderRejectedEvent")
                                .eventVersion(EVENT_VERSION)
                                .occurredOn(OffsetDateTime.now())
                                .source(EVENT_SOURCE)
                                .correlationId(correlationId)
                                .transactionId(partnerOrder.getTransactionId())
                                .payload(payload)
                                .build();
        }

        /**
         * Maps validation errors to an InvalidOrderEvent.
         * This event indicates that an order failed validation.
         *
         * @param validationErrors list of validation error messages
         * @param originalRequest  the original request that failed validation
         * @param correlationId    correlation ID for event tracing
         * @param transactionId    transaction ID for the failed request
         * @return InvalidOrderEvent ready for Kafka publishing
         */
        public InvalidOrderEvent toInvalidOrderEvent(List<String> validationErrors,
                        Object originalRequest,
                        UUID correlationId,
                        UUID transactionId) {
                log.debug("Mapping validation errors to InvalidOrderEvent - transactionId: {}, errorCount: {}",
                                transactionId, validationErrors.size());

                // Extract external ID and location code if available
                String externalId = null;
                String locationCode = null;

                if (originalRequest instanceof PartnerOrderRequest) {
                        PartnerOrderRequest request = (PartnerOrderRequest) originalRequest;
                        externalId = request.getExternalId();
                        locationCode = request.getLocationCode();
                }

                InvalidOrderEvent.InvalidOrderPayload payload = InvalidOrderEvent.InvalidOrderPayload.builder()
                                .validationErrors(validationErrors)
                                .originalRequest(originalRequest)
                                .failedAt(OffsetDateTime.now())
                                .externalId(externalId)
                                .locationCode(locationCode)
                                .build();

                return InvalidOrderEvent.builder()
                                .eventId(UUID.randomUUID())
                                .eventType("InvalidOrderEvent")
                                .eventVersion(EVENT_VERSION)
                                .occurredOn(OffsetDateTime.now())
                                .source(EVENT_SOURCE)
                                .correlationId(correlationId)
                                .transactionId(transactionId)
                                .payload(payload)
                                .build();
        }

        /**
         * Creates a test OrderRejected event for demonstrating exception handling.
         * This method is used to generate test events that will be consumed by
         * the Interface Exception Collector Service.
         *
         * @param partnerOrder  the partner order entity
         * @param correlationId correlation ID for event tracing
         * @return OrderRejected event for testing purposes
         */
        public OrderRejectedEvent createTestOrderRejectedEvent(PartnerOrder partnerOrder, UUID correlationId) {
                String testReason = "Test event for Interface Exception Collector functionality - " +
                                "demonstrating order rejection and retry workflow";

                return toOrderRejectedEvent(partnerOrder, correlationId, testReason);
        }

        /**
         * Maps a PartnerOrderItem entity to an OrderRejectedEvent.OrderItem.
         *
         * @param item the partner order item entity
         * @return mapped order item for the event
         */
        private OrderRejectedEvent.OrderItem mapToOrderRejectedItem(PartnerOrderItem item) {
                return OrderRejectedEvent.OrderItem.builder()
                                .bloodType(item.getBloodType())
                                .productFamily(item.getProductFamily())
                                .quantity(item.getQuantity())
                                .comments(item.getComments())
                                .build();
        }

        /**
         * Extracts customer ID from the partner order.
         * This is a placeholder implementation as customer ID is not currently
         * stored in the PartnerOrder entity.
         *
         * @param partnerOrder the partner order entity
         * @return customer ID or a default value
         */
        private String extractCustomerId(PartnerOrder partnerOrder) {
                // TODO: Extract customer ID from order data when available
                // For now, generate a customer ID based on location code
                return "CUST-" + partnerOrder.getLocationCode();
        }
}