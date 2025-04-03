package com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql.GraphQLMutationMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
public class CreateShipmentController {

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private SharedContext sharedContext;

    public Map createShipment(String customerCode, String productType, Float cartonTareWeight, String shipmentDate, String TransportationRefNumber, String locationCode) {
        saveLastShipmentId();
        saveLastShipmentNumber();
        String payload = GraphQLMutationMapper.createShipment(customerCode, productType, cartonTareWeight, shipmentDate, TransportationRefNumber, locationCode);
        try {
            var response = apiHelper.graphQlRequest(payload, "createShipment");
            var data = (Map) response.get("data");
            sharedContext.setShipmentCreateResponse(data);
            return data;
        } catch (Exception e) {
            log.error("Error creating shipment", e);
            throw new RuntimeException(e);
        }
    }

    public void saveLastShipmentId() {
        // Save the last shipment id in the context before creating a new one
        sharedContext.setLastShipmentId(Integer.valueOf(databaseService.fetchData(DatabaseQueries.GET_LAST_SHIPMENT_ID).first().block().get("id").toString()));
    }

    public void saveLastShipmentNumber() {
        // Save the last shipment number in the context before creating a new one
        sharedContext.setLastShipmentNumber(Integer.valueOf(databaseService.fetchData(DatabaseQueries.GET_LAST_SHIPMENT_NUMBER).first().block().get("number").toString()));
    }

    public boolean verifyIfNullNotNullOrValue(String expected, Object actual) {
        if (expected.equals("<null>")) {
            Assert.assertNull(actual);
            return true;
        } else if (expected.equals("<not_null>")) {
            Assert.assertNotNull(actual);
            return true;
        } else {
            log.info("Expected {} to be {}", expected, actual);
            return expected.equals(actual.toString());
        }
    }

    public void createShipmentWithInvalidData(String attribute, String value) {
        if (value.equals("<null>")) {
            value = null;
        } else {
            value = "\"" + value + "\"";
        }
        // default correct values
        String customerCode = "\"408\"";
        String productType = "\"RP_FROZEN_WITHIN_120_HOURS\"";
        Float cartonTareWeight = 1000f;
        String shipmentDate = "\"" + LocalDate.now().plusDays(1) + "\"";
        String TransportationRefNumber = "\"3455\"";
        String locationCode = "\"123456789\"";

        switch (attribute) {
            case "customerCode":
                customerCode = value;
                break;
            case "productType":
                productType = value;
                break;
            case "cartonTareWeight":
                if (value != null) {
                    cartonTareWeight = Float.parseFloat(value.replace("\"", ""));

                } else cartonTareWeight = null;
                break;
            case "TransportationRefNumber":
                TransportationRefNumber = "\"" + value + "\"";
                break;
            case "locationCode":
                locationCode = value;
                break;
            case "shipmentDate":
                shipmentDate = value;
                break;
            default:
                throw new RuntimeException("Invalid attribute");
        }

        try {
            createShipment(customerCode, productType, cartonTareWeight, shipmentDate, TransportationRefNumber, locationCode);
        } catch (Exception e) {
            log.info("Expected exception: {}", e.getMessage());
        }
    }

    public boolean wasNewShipmentWasCreated() {
        var lastShipmentId = sharedContext.getLastShipmentId();
        var currentShipmentId = Integer.valueOf(databaseService.fetchData(DatabaseQueries.GET_LAST_SHIPMENT_ID).first().block().get("id").toString());

        return lastShipmentId < currentShipmentId;
    }
}
