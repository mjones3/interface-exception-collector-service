package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment.SHIPMENT_DATE_RANGE_YEARS_LIMIT;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Slf4j
@Getter
@EqualsAndHashCode
@ToString
public class RecoveredPlasmaShipmentQueryCommand implements Validatable, FilterAndSortCommand {

    public static final Integer DEFAULT_PAGE_NUMBER_FIRST_PAGE = 0;
    public static final Integer DEFAULT_PAGE_SIZE = 20;
    public static final List<String> DEFAULT_STATUSES = List.of("OPEN","IN_PROGRESS");
    public static final List<QueryOrderBy> DEFAULT_SORTING = List.of(
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
        this.shipmentNumber = ofNullable(shipmentNumber)
            .map(String::trim)
            .filter(sn -> !sn.isBlank())
            .orElse(null);

        this.locationCode = locationCode;
        this.customers = customers;
        this.productTypes = productTypes;

        if (nonNull(this.shipmentNumber)) {
            this.shipmentStatus = null;
            this.shipmentDateFrom = null;
            this.shipmentDateTo = null;
        } else {
            this.shipmentStatus = isNull(shipmentStatus) || shipmentStatus.isEmpty() ? DEFAULT_STATUSES : shipmentStatus;
            if (isNull(shipmentDateFrom) && isNull(shipmentDateTo)) {
                this.shipmentDateFrom = this.getMinimalAllowedShipmentDate();
                this.shipmentDateTo = null;
            } else {
                this.shipmentDateFrom = ofNullable(shipmentDateFrom).orElseGet(this::getMinimalAllowedShipmentDate);
                this.shipmentDateTo = shipmentDateTo;
            }
        }

        this.querySort = ofNullable(querySort).orElseGet(() -> new QuerySort(DEFAULT_SORTING));
        this.pageNumber = ofNullable(pageNumber).orElse(DEFAULT_PAGE_NUMBER_FIRST_PAGE);
        this.pageSize = ofNullable(pageSize).orElse(DEFAULT_PAGE_SIZE);
        this.transportationReferenceNumber = transportationReferenceNumber;

        checkValid();
    }

    @Override
    public void checkValid() {
        if (isNull(this.shipmentNumber) && (isNull(this.locationCode) || this.locationCode.isEmpty())){
            throw new IllegalArgumentException("The locationCode must not be null or empty");
        }

        if (nonNull(this.shipmentDateFrom) && nonNull(this.shipmentDateTo)) {
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
        log.debug("OrderQueryCommand validation ran successfully {}", this);
    }

    private boolean dateRangeExceedsLimit(LocalDate from, LocalDate to) {
        var period = Period.between(from, to);
        return period.getYears() > SHIPMENT_DATE_RANGE_YEARS_LIMIT || (period.getYears() == SHIPMENT_DATE_RANGE_YEARS_LIMIT && (period.getMonths() > 0 || period.getDays() > 0));
    }

    private LocalDate getMinimalAllowedShipmentDate() {
        return LocalDate.now().minusYears(SHIPMENT_DATE_RANGE_YEARS_LIMIT);
    }

}
