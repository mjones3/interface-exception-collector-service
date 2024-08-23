package com.arcone.biopro.distribution.order.domain.model;

import graphql.Assert;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
public class OrderShipment implements Validatable {

    private Long id;
    private Long orderId;
    private Long shipmentId;
    private String shipmentStatus;
    private ZonedDateTime createDate;

    public OrderShipment(Long id, Long orderId, Long shipmentId, String shipmentStatus, ZonedDateTime createDate) {
        this.id = id;
        this.orderId = orderId;
        this.shipmentId = shipmentId;
        this.shipmentStatus = shipmentStatus;
        this.createDate = createDate;

        checkValid();
    }

    @Override
    public void checkValid() {
        Assert.assertNotNull(orderId, "Order ID cannot be null");
        Assert.assertNotNull(shipmentId, "Shipment id cannot be null");
        Assert.assertNotNull(shipmentStatus, "Shipment Status cannot be null");
        Assert.assertNotNull(createDate, "Create Date cannot be null");

    }
}
