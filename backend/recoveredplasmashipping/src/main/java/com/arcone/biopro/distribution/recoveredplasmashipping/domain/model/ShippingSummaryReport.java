package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryShipFrom;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryShipTo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ShippingSummaryReport {

    private static final String REPORT_TITLE = "Plasma Shipment Summary Report";
    private String reportTitle;
    private String employeeName;
    private String employeeId;
    private LocalDate localDate;
    private ShippingSummaryShipTo shipTo;
    private ShippingSummaryShipFrom shipFrom;
    private String testingStatement;


}
