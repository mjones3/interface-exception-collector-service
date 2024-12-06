package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;


import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@EqualsAndHashCode
@ToString
public class OrderQueryCommand implements Validatable {

    private String locationCode;
    private String orderNumber;
    private String externalOrderId;
    private List<String> orderStatus;
    private List<String> deliveryTypes;
    private List<String> customers;
    private LocalDate createDateFrom;
    private LocalDate createDateTo;
    private LocalDate desireShipDateFrom;
    private LocalDate desireShipDateTo;
    private QuerySort querySort;
    private Integer limit;

    private static final String DEFAULT_SORT_DIRECTION = "ASC";
    private static final String DEFAULT_FIRST_SORT_BY = "priority";
    private static final String DEFAULT_SECOND_SORT_BY = "status";
    private static final String DEFAULT_THIRD_SORT_BY = "desired_shipping_date";
    private static final Integer DEFAULT_LIMIT = 20;
    private static final List<String> DEFAULT_STATUSES = List.of("OPEN", "IN_PROGRESS");

    public OrderQueryCommand(
        String locationCode,
        String orderUniqueIdentifier,
        List<String> orderStatus,
        List<String> deliveryTypes,
        List<String> customers,
        LocalDate createDateFrom,
        LocalDate createDateTo,
        LocalDate desireShipDateFrom,
        LocalDate desireShipDateTo,
        QuerySort querySort, Integer limit) {

        this.locationCode = locationCode;
        this.querySort = querySort;
        this.limit = limit;
        if (this.limit == null) {
            this.limit = DEFAULT_LIMIT;
        }
        if (this.querySort == null) {
            this.querySort = new QuerySort(List.of(new QueryOrderBy(DEFAULT_FIRST_SORT_BY, DEFAULT_SORT_DIRECTION),
                new QueryOrderBy(DEFAULT_SECOND_SORT_BY, DEFAULT_SORT_DIRECTION),
                new QueryOrderBy(DEFAULT_THIRD_SORT_BY, DEFAULT_SORT_DIRECTION)));
        }

        try {
            var _orderNumber = Long.parseLong(orderUniqueIdentifier);
            this.orderNumber = orderUniqueIdentifier;
            this.externalOrderId = orderUniqueIdentifier;
        } catch (NumberFormatException e) {
            this.externalOrderId = orderUniqueIdentifier;
        }

        this.orderStatus = orderStatus;
        if (orderStatus == null) {
            this.orderStatus = DEFAULT_STATUSES;
        }
        this.deliveryTypes = deliveryTypes;
        this.customers = customers;
        this.createDateFrom = createDateFrom;
        this.createDateTo = createDateTo;
        this.desireShipDateFrom = desireShipDateFrom;
        this.desireShipDateTo = desireShipDateTo;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.locationCode == null || this.locationCode.isEmpty()) {
            throw new IllegalArgumentException("locationCode cannot be null or empty");
        }

        if ((Objects.nonNull(this.orderNumber) || Objects.nonNull(this.externalOrderId)) && (Objects.nonNull(this.createDateFrom) || Objects.nonNull(this.createDateTo))) {
            throw new IllegalArgumentException("The createDate must be null or empty");
        }

        if ((Objects.isNull(this.orderNumber)
            && Objects.isNull(this.externalOrderId))
            && (Objects.isNull(this.createDateFrom)
            && Objects.isNull(this.createDateTo))
            && ((Objects.nonNull(this.orderStatus) && !this.orderStatus.isEmpty() && (this.orderStatus.size() != DEFAULT_STATUSES.size() || !this.orderStatus.containsAll(DEFAULT_STATUSES)))
            || (Objects.nonNull(this.deliveryTypes) && !this.deliveryTypes.isEmpty())
            || (Objects.nonNull(this.customers) && !this.customers.isEmpty())
            || (Objects.nonNull(this.desireShipDateFrom) || Objects.nonNull(this.desireShipDateTo)))) {
            throw new IllegalArgumentException("The createDate must not be null or empty");
        }

        if (Objects.nonNull(this.createDateFrom) && Objects.nonNull(this.createDateTo) && this.createDateTo.isBefore(this.createDateFrom)) {
            throw new IllegalArgumentException("Initial date should not be greater than final date");
        }

        if (Objects.nonNull(this.createDateTo) && this.createDateTo.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Final date should not be greater than today");
        }

        if (Objects.nonNull(this.createDateFrom) && this.createDateFrom.isBefore(LocalDate.now().minusYears(2))) {
            throw new IllegalArgumentException("Date range exceeds two years");
        }

        Assert.notNull(this.querySort, "Sort must not be null");

        if (this.limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
    }
}
