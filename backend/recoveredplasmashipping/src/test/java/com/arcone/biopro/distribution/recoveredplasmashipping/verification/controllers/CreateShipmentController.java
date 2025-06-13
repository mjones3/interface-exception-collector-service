package com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql.GraphQLMutationMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql.GraphQLQueryMapper;
import io.cucumber.spring.ScenarioScope;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ScenarioScope
public class CreateShipmentController {

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private TestUtils testUtils;

    public Map createShipment(String customerCode, String productType, Float cartonTareWeight, String shipmentDate, String transportationRefNumber, String locationCode) {
        saveLastShipmentId();
        saveLastShipmentNumber();
        sharedContext.setLocationCode(locationCode);
        transportationRefNumber = transportationRefNumber.equals("<null>") ? null : "\"" + transportationRefNumber + "\"";
        shipmentDate = shipmentDate == null ? null : "\"" + shipmentDate + "\"";

        String payload = GraphQLMutationMapper.createShipment(
            "\"" + customerCode + "\"",
            "\"" + productType + "\"",
            cartonTareWeight,
            shipmentDate,
            transportationRefNumber,
            "\"" + locationCode + "\"");
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
            value = value;
        }
        // default correct values
        String customerCode = "408";
        String productType = "RP_FROZEN_WITHIN_120_HOURS";
        Float cartonTareWeight = 1000f;
        String shipmentDate = LocalDate.now().plusDays(1).toString();
        String TransportationRefNumber = "3455";
        String locationCode = "123456789";

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
                TransportationRefNumber = value;
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

    public Map closeShipment(String id, String employeeId, String locationCode , String shipDate ) {
        String payload = GraphQLMutationMapper.closeShipment(id, employeeId, locationCode ,shipDate);
        var response = apiHelper.graphQlRequest(payload, "closeShipment");
        sharedContext.setLastShipmentCloseResponse((Map) response);
        if (response.get("data") != null) {
            sharedContext.setShipmentCreateResponse((Map) response.get("data"));
        }
        if(response.get("errors") != null){
            sharedContext.setApiErrorResponse((Map) ((List) response.get("errors")).getFirst());
        }
        return response;
    }

    public void printUnacceptableUnitsReport(String shipmentId, String locationCode) {
        String payload = GraphQLQueryMapper.printUnacceptalbleUnitsReport(Integer.parseInt(shipmentId), sharedContext.getEmployeeId(),locationCode);
        var response = apiHelper.graphQlRequest(payload, "printUnacceptableUnitsReport");
        sharedContext.setLastUnacceptableUnitsReportResponse((Map) response.get("data"));
    }

    public void updateShipmentStatus(String shipmentId, String status) {
        databaseService.executeSql(DatabaseQueries.UPDATE_SHIPMENT_STATUS(shipmentId, status)).block();
    }

    public Map printShippingSummaryReport(String shipmentId, String locationCode) {
        String payload = GraphQLQueryMapper.printShippingSummaryReport(Integer.parseInt(shipmentId), sharedContext.getEmployeeId(),locationCode);
        var response = apiHelper.graphQlRequest(payload, "printShippingSummaryReport");
        sharedContext.setLastShippingSummaryReportResponse((Map) response.get("data"));
        return response;
    }


    public Map modifyShipment(int shipmentId, String customerCode, String productType, String transpRefNumber, String shipmentDate, int cartonTareWeight, String employeeId, String comments) {
        shipmentDate = testUtils.parseDateKeyword(shipmentDate);
        transpRefNumber = transpRefNumber.equalsIgnoreCase("<null>") ? null : "\"" + transpRefNumber + "\"";
        comments = comments.equalsIgnoreCase("<null>") ? null : "\"" + comments + "\"";

        String payload = GraphQLMutationMapper.modifyShipment(shipmentId, customerCode, productType, transpRefNumber, shipmentDate, cartonTareWeight, employeeId, comments);
        var response = apiHelper.graphQlRequest(payload, "modifyShipment");
        sharedContext.setLastShipmentModifyResponse((Map) response.get("data"));
        return response;
    }

    public void requestShipmentModifyHistory(int id) {
        String payload = GraphQLQueryMapper.requestShipmentModifyHistory(id);
        var response = apiHelper.graphQlListRequest(payload, "findAllShipmentHistoryByShipmentId");
        sharedContext.setLastShipmentModifyHistoryResponse(response);
    }
}
