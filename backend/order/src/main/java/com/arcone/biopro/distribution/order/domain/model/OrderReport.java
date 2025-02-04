package com.arcone.biopro.distribution.order.domain.model;

import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriorityReport;
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

    private Long orderId;
    private Long orderNumber;
    private String externalId;
    private String orderStatus;
    private OrderCustomerReport orderCustomerReport;
    private OrderPriorityReport orderPriorityReport;
    private ZonedDateTime createDate;
    private LocalDate desireShipDate;


    public OrderReport(Long orderId, Long orderNumber, String externalId, OrderCustomerReport orderCustomerReport, OrderPriorityReport orderPriorityReport
        ,String orderStatus, ZonedDateTime createDate, LocalDate desireShipDate) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.externalId = externalId;
        this.orderStatus = orderStatus;
        this.orderCustomerReport = orderCustomerReport;
        this.orderPriorityReport = orderPriorityReport;
        this.createDate = createDate;
        this.desireShipDate = desireShipDate;

        checkValid();
    }


    @Override
    public void checkValid() {
        Assert.notNull(orderId, "orderId must not be null");
        Assert.notNull(orderNumber, "orderNumber must not be null");
        Assert.notNull(externalId, "externalId must not be null");
        Assert.notNull(orderCustomerReport, "orderCustomerReport must not be null");
        Assert.notNull(orderPriorityReport, "orderPriorityReport must not be null");
        Assert.notNull(createDate, "createDate must not be null");
        Assert.notNull(orderStatus, "orderStatus must not be null");
    }
}
