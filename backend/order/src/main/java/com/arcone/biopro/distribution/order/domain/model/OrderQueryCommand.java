package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;

@Slf4j
@Getter
@EqualsAndHashCode
@ToString
public class OrderQueryCommand implements Validatable, FilterAndSortCommand {

    private static final Integer DEFAULT_PAGE_NUMBER_FIRST_PAGE = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 20;
    private static final List<String> DEFAULT_STATUSES = List.of("OPEN", "IN_PROGRESS");
    private static final List<QueryOrderBy> DEFAULT_SORTING = List.of(
        new QueryOrderBy("priority", "ASC"),
        new QueryOrderBy("status", "DESC"),
        new QueryOrderBy("desired_shipping_date", "ASC")
    );

    private final String locationCode;
    private final String orderNumber;
    private final String externalOrderId;
    private final List<String> orderStatus;
    private final List<String> deliveryTypes;
    private final List<String> customers;
    private final LocalDate createDateFrom;
    private final LocalDate createDateTo;
    private final LocalDate desireShipDateFrom;
    private final LocalDate desireShipDateTo;
    private final QuerySort querySort;
    private final Integer pageNumber;
    private final Integer pageSize;

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
        QuerySort querySort,
        Integer pageNumber,
        Integer pageSize
    ) {
        this.locationCode = locationCode;

        String orderNumberParsed = null;
        try {
            orderNumberParsed = String.valueOf(Long.parseLong(orderUniqueIdentifier));
        } catch (NumberFormatException e) {
            log.debug("Unable to parse order number from order unique identifier: {}", orderUniqueIdentifier);
        }
        this.orderNumber = orderNumberParsed;

        this.externalOrderId = orderUniqueIdentifier;
        this.orderStatus = orderStatus == null && this.externalOrderId == null
            ? DEFAULT_STATUSES
            : orderStatus;

        this.deliveryTypes = deliveryTypes;
        this.customers = customers;
        this.createDateFrom = createDateFrom;
        this.createDateTo = createDateTo;
        this.desireShipDateFrom = desireShipDateFrom;
        this.desireShipDateTo = desireShipDateTo;
        this.querySort = ofNullable(querySort).orElseGet(() -> new QuerySort(DEFAULT_SORTING));
        this.pageNumber = ofNullable(pageNumber).orElse(DEFAULT_PAGE_NUMBER_FIRST_PAGE);
        this.pageSize = ofNullable(pageSize).orElse(DEFAULT_PAGE_SIZE);

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

        if (Objects.isNull(this.querySort)) {
            throw new IllegalArgumentException("Sort must not be null");
        }

        if (this.pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }

        if (this.pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be greater than or equal to 0");
        }
        log.debug("OrderQueryCommand validation ran successfully! {}", this);
    }
}
