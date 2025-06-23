package com.arcone.biopro.distribution.eventbridge.domain.model;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderPayload;
import lombok.Getter;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
public class OrderOutbound {

    private final Integer orderNumber;
    private final String externalId;
    private final String orderStatus;
    private final String locationCode;
    private final Instant createDate;
    private final String createEmployeeCode;
    private final String shipmentType;
    private final String priority;
    private final String shippingMethod;
    private final String productCategory;
    private final LocalDate desiredShippingDate;
    private final String shippingCustomerCode;
    private final String billingCustomerCode;
    private final String comments;
    private final Boolean willPickUp;
    private final String willPickUpPhoneNumber;
    private final Integer totalShipped;
    private final Integer totalRemaining;
    private final Integer totalProducts;
    private final List<OrderPayload.OrderItem> orderItems;
    private final String cancelEmployeeId;
    private final Instant cancelDate;
    private final String cancelReason;
    private final String modifyEmployeeId;
    private final Instant modifyDate;
    private final String modifyReason;
    private final String transactionId;

    public OrderOutbound(Integer orderNumber, String externalId, String orderStatus,
                         String locationCode, Instant createDate, String createEmployeeCode,
                         String shipmentType, String priority, String shippingMethod,
                         String productCategory, LocalDate desiredShippingDate,
                         String shippingCustomerCode, String billingCustomerCode,
                         String comments, Boolean willPickUp, String willPickUpPhoneNumber,
                         Integer totalShipped, Integer totalRemaining, Integer totalProducts,
                         List<OrderPayload.OrderItem> orderItems, String cancelEmployeeId,
                         Instant cancelDate, String cancelReason, String modifyEmployeeId,
                         Instant modifyDate, String modifyReason, String transactionId) {

        Assert.notNull(orderNumber, "orderNumber must not be null");
        Assert.notNull(externalId, "externalId must not be null");
        Assert.notNull(orderStatus, "orderStatus must not be null");
        Assert.notNull(locationCode, "locationCode must not be null");
        Assert.notNull(createDate, "createDate must not be null");
        Assert.notNull(createEmployeeCode, "createEmployeeCode must not be null");
        Assert.notNull(shipmentType, "shipmentType must not be null");
        Assert.notNull(priority, "priority must not be null");
        Assert.notNull(shippingMethod, "shippingMethod must not be null");
        Assert.notNull(productCategory, "productCategory must not be null");
        Assert.notNull(shippingCustomerCode, "shippingCustomerCode must not be null");
        Assert.notNull(billingCustomerCode, "billingCustomerCode must not be null");
        Assert.notNull(willPickUp, "willPickUp must not be null");
        Assert.notNull(orderItems, "orderItems must not be null");

        this.orderNumber = orderNumber;
        this.externalId = externalId;
        this.orderStatus = orderStatus;
        this.locationCode = locationCode;
        this.createDate = createDate;
        this.createEmployeeCode = createEmployeeCode;
        this.shipmentType = shipmentType;
        this.priority = priority;
        this.shippingMethod = shippingMethod;
        this.productCategory = productCategory;
        this.desiredShippingDate = desiredShippingDate;
        this.shippingCustomerCode = shippingCustomerCode;
        this.billingCustomerCode = billingCustomerCode;
        this.comments = comments;
        this.willPickUp = willPickUp;
        this.willPickUpPhoneNumber = willPickUpPhoneNumber;
        this.totalShipped = totalShipped;
        this.totalRemaining = totalRemaining;
        this.totalProducts = totalProducts;
        this.orderItems = orderItems;
        this.cancelEmployeeId = cancelEmployeeId;
        this.cancelDate = cancelDate;
        this.cancelReason = cancelReason;
        this.modifyEmployeeId = modifyEmployeeId;
        this.modifyDate = modifyDate;
        this.modifyReason = modifyReason;
        this.transactionId = transactionId;
    }
}