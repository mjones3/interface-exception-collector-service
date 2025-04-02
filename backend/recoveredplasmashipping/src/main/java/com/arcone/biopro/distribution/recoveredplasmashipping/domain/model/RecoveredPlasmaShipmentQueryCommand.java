package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

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
public class RecoveredPlasmaShipmentQueryCommand implements Validatable, FilterAndSortCommand {

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

    public RecoveredPlasmaShipmentQueryCommand(
        List<String> locationCode,
        String shipmentNumber,
        List<String> shipmentStatus,
        List<String> customers,
        List<String> productTypes,
        LocalDate createDateFrom,
        LocalDate createDateTo,
        QuerySort querySort,
        Integer pageNumber,
        Integer pageSize
    ) {
        this.locationCode = locationCode;
        this.shipmentNumber = shipmentNumber;


        this.shipmentStatus = shipmentStatus == null && (this.shipmentNumber == null || this.shipmentNumber.isBlank())
            ? DEFAULT_STATUSES
            : shipmentStatus;

        this.customers = customers;
        this.productTypes = productTypes;
        this.shipmentDateFrom = createDateFrom;
        this.shipmentDateTo = createDateTo;
        this.querySort = ofNullable(querySort).orElseGet(() -> new QuerySort(DEFAULT_SORTING));
        this.pageNumber = ofNullable(pageNumber).orElse(DEFAULT_PAGE_NUMBER_FIRST_PAGE);
        this.pageSize = ofNullable(pageSize).orElse(DEFAULT_PAGE_SIZE);

        checkValid();
    }

    @Override
    public void checkValid() {

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

        if (Objects.nonNull(this.shipmentDateFrom) && Objects.nonNull(this.shipmentDateTo) && this.shipmentDateTo.isBefore(this.shipmentDateFrom)) {
            throw new IllegalArgumentException("Initial date should not be greater than final date");
        }

        if (Objects.nonNull(this.shipmentDateTo) && this.shipmentDateTo.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Final date should not be greater than today");
        }

        if (Objects.nonNull(this.shipmentDateFrom) && this.shipmentDateFrom.isBefore(LocalDate.now().minusYears(2))) {
            throw new IllegalArgumentException("Date range exceeds two years");
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
