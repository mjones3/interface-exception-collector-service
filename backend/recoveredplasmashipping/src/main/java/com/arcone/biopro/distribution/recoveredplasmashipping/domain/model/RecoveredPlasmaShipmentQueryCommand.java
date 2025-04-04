package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;

@Slf4j
@Getter
@EqualsAndHashCode
@ToString
public class RecoveredPlasmaShipmentQueryCommand implements Validatable, FilterAndSortCommand {

    private static final int SHIPMENT_DATE_RANGE_YEARS_LIMIT = 2;
    private static final Integer DEFAULT_PAGE_NUMBER_FIRST_PAGE = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 20;
    private static final List<String> DEFAULT_STATUSES = List.of("OPEN");
    private static final List<QueryOrderBy> DEFAULT_SORTING = List.of(
        new QueryOrderBy("status", "DESC"),
        new QueryOrderBy("shipment_date", "ASC"),
        new QueryOrderBy("id", "DESC")
    );

    private final List<String> locationCode;
    private final String shipmentNumber;
    private final List<String> shipmentStatus;
    private final List<String> customers;
    private final List<String> productTypes;
    private final LocalDate shipmentDateFrom;
    private final LocalDate shipmentDateTo;
    private final QuerySort querySort;
    private final Integer pageNumber;
    private final Integer pageSize;
    private final String transportationReferenceNumber;

    public RecoveredPlasmaShipmentQueryCommand(
        List<String> locationCode,
        String shipmentNumber,
        List<String> shipmentStatus,
        List<String> customers,
        List<String> productTypes,
        LocalDate shipmentDateFrom,
        LocalDate shipmentDateTo,
        QuerySort querySort,
        Integer pageNumber,
        Integer pageSize,
        String transportationReferenceNumber
    ) {
        this.locationCode = locationCode;
        this.shipmentNumber = shipmentNumber;


        this.shipmentStatus = shipmentStatus == null && (this.shipmentNumber == null || this.shipmentNumber.isBlank())
            ? DEFAULT_STATUSES
            : shipmentStatus;

        this.customers = customers;
        this.productTypes = productTypes;
        this.shipmentDateFrom = shipmentDateFrom;
        this.shipmentDateTo = shipmentDateTo;
        this.querySort = ofNullable(querySort).orElseGet(() -> new QuerySort(DEFAULT_SORTING));
        this.pageNumber = ofNullable(pageNumber).orElse(DEFAULT_PAGE_NUMBER_FIRST_PAGE);
        this.pageSize = ofNullable(pageSize).orElse(DEFAULT_PAGE_SIZE);
        this.transportationReferenceNumber = transportationReferenceNumber;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (Objects.isNull(this.shipmentNumber) && (Objects.isNull(this.locationCode) || this.locationCode.isEmpty())){
            throw new IllegalArgumentException("The locationCode must not be null or empty");
        }

        if (Objects.nonNull(this.shipmentNumber) && (Objects.nonNull(this.shipmentDateFrom) || Objects.nonNull(this.shipmentDateTo))) {
            throw new IllegalArgumentException("The shipmentDate must be null or empty");
        }

        if ((Objects.isNull(this.shipmentNumber)
            && (Objects.isNull(this.shipmentDateFrom)
            && Objects.isNull(this.shipmentDateTo))
            && ((Objects.nonNull(this.shipmentStatus) && !this.shipmentStatus.isEmpty() && (this.shipmentStatus.size() != DEFAULT_STATUSES.size() || !this.shipmentStatus.containsAll(DEFAULT_STATUSES)))
            || (Objects.nonNull(this.customers) && !this.customers.isEmpty())
            ))) {
            throw new IllegalArgumentException("The shipmentDate must not be null or empty");
        }

        if (Objects.nonNull(this.shipmentDateFrom) && Objects.nonNull(this.shipmentDateTo)) {
            if (this.shipmentDateTo.isBefore(this.shipmentDateFrom)) {
                throw new IllegalArgumentException("Initial date should not be greater than final date");
            }
            if (dateRangeExceedsLimit(this.shipmentDateFrom, this.shipmentDateTo)) {
                throw new IllegalArgumentException("Shipment date range exceeds " + SHIPMENT_DATE_RANGE_YEARS_LIMIT + " years");
            }
        }

        if (this.pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }

        if (this.pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be greater than or equal to 0");
        }
        log.debug("OrderQueryCommand validation ran successfully! {}", this);
    }

    private boolean dateRangeExceedsLimit(LocalDate from, LocalDate to) {
        var period = Period.between(from, to);
        return period.getYears() > SHIPMENT_DATE_RANGE_YEARS_LIMIT || (period.getYears() == SHIPMENT_DATE_RANGE_YEARS_LIMIT && (period.getMonths() > 0 || period.getDays() > 0));
    }

}
