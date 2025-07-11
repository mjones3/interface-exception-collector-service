package com.arcone.biopro.distribution.order.domain.model;

import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriorityReport;
import com.arcone.biopro.distribution.order.domain.model.vo.ShipmentType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
public class OrderReport implements Validatable {

    private final Long orderId;
    private final Long orderNumber;
    private final String externalId;
    private final String orderStatus;
    private final OrderCustomerReport orderCustomerReport;
    private final OrderPriorityReport orderPriorityReport;
    private final ZonedDateTime createDate;
    private final LocalDate desireShipDate;
    private final String shipmentType;

    public OrderReport(Long orderId, Long orderNumber, String externalId, String orderStatus, OrderCustomerReport orderCustomerReport, OrderPriorityReport orderPriorityReport, ZonedDateTime createDate, LocalDate desireShipDate , String shipmentType) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.externalId = externalId;
        this.orderStatus = orderStatus;
        this.orderCustomerReport = orderCustomerReport;
        this.orderPriorityReport = orderPriorityReport;
        this.createDate = createDate;
        this.desireShipDate = desireShipDate;
        this.shipmentType = shipmentType;
        checkValid();
    }

    @Override
    public void checkValid() {
        Assert.notNull(orderId, "orderId must not be null");
        Assert.notNull(orderNumber, "orderNumber must not be null");
        Assert.notNull(externalId, "externalId must not be null");
        Assert.notNull(orderPriorityReport, "orderPriorityReport must not be null");
        Assert.notNull(createDate, "createDate must not be null");
        Assert.notNull(orderStatus, "orderStatus must not be null");
        Assert.notNull(shipmentType, "shipmentType must not be null");
    }

}
