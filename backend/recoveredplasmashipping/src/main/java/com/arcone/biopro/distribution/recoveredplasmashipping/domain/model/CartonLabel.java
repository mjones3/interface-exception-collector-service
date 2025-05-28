package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@Builder(access = AccessLevel.PRIVATE)
public class CartonLabel implements Validatable {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final String CARTON_SEQUENCE_TXT = "Carton Sequence in Shipment %s";
    private static final String CARTON_SEQUENCE_TOTAL_CARTONS_TXT = "Carton Sequence in Shipment %s of ____";

    private ShipmentCustomer shipmentCustomer;
    private String cartonNumber;
    private Integer cartonSequenceNumber;
    private ZonedDateTime cartonCloseDate;
    private String bloodCenterName;
    private Location location;
    private String transportationReferenceNumber;
    private String shipmentNumber;
    private String productCode;
    private boolean displayTransportationNumber;
    private boolean displayTotalCartons;

    public CartonLabel(ShipmentCustomer shipmentCustomer, String cartonNumber, Integer cartonSequenceNumber, ZonedDateTime cartonCloseDate
        , String bloodCenterName, Location location , String transportationReferenceNumber, String shipmentNumber, String productCode , boolean displayTransportationNumber , boolean displayTotalCartons) {
        this.shipmentCustomer = shipmentCustomer;
        this.cartonNumber = cartonNumber;
        this.cartonSequenceNumber = cartonSequenceNumber;
        this.cartonCloseDate = cartonCloseDate;
        this.bloodCenterName = bloodCenterName;
        this.location = location;
        this.transportationReferenceNumber = transportationReferenceNumber;
        this.shipmentNumber = shipmentNumber;
        this.productCode = productCode;
        this.displayTransportationNumber = displayTransportationNumber;
        this.displayTotalCartons = displayTotalCartons;
        checkValid();
    }

    public Map<String, Object> toMap() {
        var values = new HashMap<String, Object>();
        upperLeftQuadrant(values);
        upperRightQuadrant(values);
        lowerQuadrant(values);
        return values;
    }


    private void upperRightQuadrant(HashMap<String, Object> values) {

        values.put("BLOOD_CENTER_NAME", bloodCenterName);
        values.put("ADDRESS_LINE", location.getAddressLine1());
        values.put("CITY", location.getCity());
        values.put("STATE", location.getState());
        values.put("ZIPCODE", location.getPostalCode());
        values.put("COUNTRY", "USA");
        values.put("TRANSPORTATION_NUMBER", transportationReferenceNumber);
        if(displayTransportationNumber){
            values.put("DISPLAY_TRANSPORTATION_NUMBER","Y");
        }
    }

    private void upperLeftQuadrant(Map<String, Object> values) {
        values.put("CUSTOMER_CODE", shipmentCustomer.getCustomerCode());
        values.put("CUSTOMER_NAME", shipmentCustomer.getCustomerName());
        values.put("CUSTOMER_ADDRESS", shipmentCustomer.getCustomerAddressLine1());
        values.put("CUSTOMER_CITY", shipmentCustomer.getCustomerCity());
        values.put("CUSTOMER_STATE", shipmentCustomer.getCustomerState());
        values.put("CUSTOMER_ZIP_CODE", shipmentCustomer.getCustomerPostalCode());
        values.put("CUSTOMER_COUNTRY", shipmentCustomer.getCustomerCountry());
        values.put("CARTON_NUMBER", cartonNumber);
        values.put("CLOSE_DATE", cartonCloseDate.format(DATE_FORMATTER));
    }

    private void lowerQuadrant(HashMap<String, Object> values) {
        values.put("PRODUCT_CODE", productCode);
        if(displayTotalCartons){
            values.put("CARTON_SEQUENCE", String.format(CARTON_SEQUENCE_TOTAL_CARTONS_TXT, cartonSequenceNumber));
        }else{
            values.put("CARTON_SEQUENCE", String.format(CARTON_SEQUENCE_TXT, cartonSequenceNumber));
        }
        values.put("SHIPMENT_NUMBER", shipmentNumber);
    }

    @Override
    public void checkValid() {
        if (shipmentCustomer == null) {
            throw new IllegalArgumentException("Shipment Customer is required");
        }
        if (cartonNumber == null || cartonNumber.isBlank()) {
            throw new IllegalArgumentException("Carton Number is required");
        }
        if (cartonSequenceNumber == null) {
            throw new IllegalArgumentException("Carton Sequence Number is required");
        }
        if (cartonCloseDate == null) {
            throw new IllegalArgumentException("Carton Close Date is required");
        }
        if (bloodCenterName == null || bloodCenterName.isBlank()) {
            throw new IllegalArgumentException("Blood Center Name is required");
        }

        if (location == null) {
            throw new IllegalArgumentException("Location is required");
        }

        if (shipmentNumber == null || shipmentNumber.isBlank()) {
            throw new IllegalArgumentException("Shipment Number is required");
        }
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product Code is required");
        }

    }
}
