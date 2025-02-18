package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

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
        Assert.notNull(orderId, "Order ID cannot be null");
        Assert.notNull(shipmentId, "Shipment id cannot be null");
        Assert.notNull(shipmentStatus, "Shipment Status cannot be null");
        Assert.notNull(createDate, "Create Date cannot be null");

    }

    public void setShipmentStatus(String status){
        this.shipmentStatus = status;
    }
}
