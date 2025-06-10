package com.arcone.biopro.distribution.eventbridge.domain.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class RecoveredPlasmaShipmentClosedOutbound implements Validatable {

    private String shipmentNumber;
    private String locationShipmentCode;
    private String locationCartonCode;
    private String customerCode;
    private LocalDate shipmentDate;
    private String shipmentDateFormatted;
    @Getter(AccessLevel.NONE)
    private int totalShipmentProducts;
    private String bloodCenterName;
    private String shipmentLocationCode;
    private ZonedDateTime closeDate;
    private String closeDateFormatted;
    private List<RecoveredPlasmaShipmentClosedCartonOutbound> cartonOutboundList;

    private static final String DEFAULT_BLOOD_CENTER_NAME = "ARC";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    public RecoveredPlasmaShipmentClosedOutbound(String shipmentNumber, String locationShipmentCode, String locationCartonCode, String customerCode, LocalDate shipmentDate , ZonedDateTime closeDate, String shipmentLocationCode) {
        this.shipmentNumber = shipmentNumber;
        this.locationShipmentCode = locationShipmentCode;
        this.locationCartonCode = locationCartonCode;
        this.customerCode = customerCode;
        this.shipmentDate = shipmentDate;
        this.bloodCenterName = DEFAULT_BLOOD_CENTER_NAME;
        this.shipmentLocationCode = shipmentLocationCode;
        this.closeDate = closeDate;

        if(shipmentDate != null){
            this.shipmentDateFormatted = shipmentDate.format(java.time.format.DateTimeFormatter.ofPattern(DATE_FORMAT));
        }

        if(closeDate != null){
            this.closeDateFormatted = closeDate.format(java.time.format.DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        }

        this.checkValid();
    }

    public void addCarton(final String cartonNumber , final int totalProducts , final List<RecoveredPlasmaShipmentClosedCartonItemOutbound> packedProducts){
        if(this.cartonOutboundList == null){
            this.cartonOutboundList = new ArrayList<>();
        }
        this.cartonOutboundList.add(new RecoveredPlasmaShipmentClosedCartonOutbound(cartonNumber,totalProducts,packedProducts));

    }

    @Override
    public void checkValid() {
        if (shipmentNumber == null || shipmentNumber.isBlank()) {
            throw new IllegalStateException("Shipment number is null");
        }

        if (locationShipmentCode == null || locationShipmentCode.isBlank()) {
            throw new IllegalStateException("Location shipment code is null");
        }

        if (locationCartonCode == null || locationCartonCode.isBlank()) {
            throw new IllegalStateException("Location carton code is null");
        }

        if (customerCode == null || customerCode.isBlank()) {
            throw new IllegalStateException("Customer code is null");
        }
    }


    public int getTotalShipmentProducts() {
        if (cartonOutboundList != null) {
            return cartonOutboundList.stream()
                    .mapToInt(RecoveredPlasmaShipmentClosedCartonOutbound::getTotalProducts)
                    .sum();
        }

        return 0;

    }
}
