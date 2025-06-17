package com.arcone.biopro.distribution.receiving.verification.controllers;

import com.arcone.biopro.distribution.receiving.verification.support.ApiHelper;
import com.arcone.biopro.distribution.receiving.verification.support.SharedContext;
import com.arcone.biopro.distribution.receiving.verification.support.graphql.GraphQLMutationMapper;
import com.arcone.biopro.distribution.receiving.verification.support.graphql.GraphQLQueryMapper;
import io.cucumber.spring.ScenarioScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ScenarioScope
public class ImportProductsController {

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private SharedContext sharedContext;

    @Value("${default.employee.id}")
    private String employeeId;


    public Map enterShippingInformation(String temperatureCategory, String locationCode) {

        String payload = GraphQLQueryMapper.enterShippingInformation(temperatureCategory, employeeId, locationCode);
        var response = apiHelper.graphQlRequest(payload, "enterShippingInformation");
        log.debug("Response: {}", response);
        return response;
    }

    public boolean isTemperatureValid(String temperatureCategory, String temperatureValue) {
        String payload = GraphQLQueryMapper.validateTemperature(temperatureCategory, temperatureValue);
        var response = apiHelper.graphQlRequest(payload, "validateTemperature");

        return Boolean.parseBoolean(((Map) response.get("data")).get("valid").toString());
    }

    public boolean isTotalTransitTimeValid(String temperatureCategory, String startDateTime, String startTimeZone, String endDateTime, String endTimeZone) {
        String payload = GraphQLQueryMapper.validateTransitTime(temperatureCategory, startDateTime, startTimeZone, endDateTime, endTimeZone);
        var response = apiHelper.graphQlRequest(payload, "validateTransitTime");

        if (response.get("data") != null) {
            boolean isValid = Boolean.parseBoolean(((Map) response.get("data")).get("valid").toString());
            sharedContext.setTotalTransitTime(((Map) response.get("data")).get("resultDescription").toString());
            return isValid;
        } else {
            return false;
        }
    }

    public String getTotalTransitTime() {
        return sharedContext.getTotalTransitTime();
    }

    public void createImportedBatch(String temperatureCategory, String transitStartDateTime, String transitStartTimeZone, String transitEndDateTime, String transitEndTimeZone, String temperature, String thermometerCode, String locationCode, String comments, String employeeId) {
        String payload = GraphQLMutationMapper.createImportMutation(temperatureCategory, transitStartDateTime, transitStartTimeZone, transitEndDateTime, transitEndTimeZone, temperature, thermometerCode, locationCode, comments, employeeId);
        var response = apiHelper.graphQlRequest(payload, "createImport");
        log.debug("Create batch response: {}", response);
        sharedContext.setCreateImportResponse((Map) response.get("data"));
        if(sharedContext.getCreateImportResponse() != null){
            sharedContext.setLastImportId(sharedContext.getCreateImportResponse().get("id").toString());
        }
    }

    public void createImportItem(String unitNumber, String productCode, String bloodType, String expirationDate, String licenseStatus, String visualInspection) {
        String payload = GraphQLMutationMapper.createImportItemMutation(
            sharedContext.getCreateImportResponse().get("id").toString(),
            unitNumber,
            productCode,
            bloodType,
            expirationDate,
            visualInspection,
            licenseStatus,
            employeeId
        );
        var response = apiHelper.graphQlRequest(payload, "createImportItem");
        log.debug("Create import item response: {}", response);
        if (response.get("data") != null) {
            sharedContext.setCreateImportItemResponse((Map) response.get("data"));
        } else {
            sharedContext.setCreateImportItemResponse(null);
        }
    }

    public boolean isUnitImported(String unitNumber) {
        if (sharedContext.getCreateImportItemResponse() == null) {
            return false;
        }
        List<Map> importedProducts = (List<Map>) sharedContext.getCreateImportItemResponse().get("products");
        if (importedProducts == null) {
            return false;
        }

        boolean isUnitImported = false;
        for (Map product : importedProducts) {
            if (product.get("unitNumber").toString().equals(unitNumber)) {
                isUnitImported = true;
            }
        }

        log.debug("Is unit imported: {}", isUnitImported);
        return isUnitImported;
    }

    public boolean isImportedUnitQuarantined(String unitNumber) {
        if (sharedContext.getCreateImportItemResponse() == null) {
            return false;
        }
        List<Map> importedProducts = (List<Map>) sharedContext.getCreateImportItemResponse().get("products");
        if (importedProducts == null) {
            return false;
        }

        boolean isImportedUnitQuarantined = false;
        for (Map product : importedProducts) {
            if (product.get("unitNumber").toString().equals(unitNumber)) {
                isImportedUnitQuarantined = Boolean.parseBoolean(product.get("isQuarantined").toString());
            }
        }

        log.debug("Is unit quarantined: {}", isImportedUnitQuarantined);
        return isImportedUnitQuarantined;
    }

    public Map validateBarcode(String temperatureCategory,String barcodePattern, String barcodeValue) {
        String payload = GraphQLQueryMapper.validateBarcodeValue(temperatureCategory,barcodePattern,barcodeValue);
        var response = apiHelper.graphQlRequest(payload, "validateBarcode");
        log.debug("validate Barcode response: {}", response);
        return response;
    }

    public Map completeImport() {
        String payload = GraphQLMutationMapper.completeImportMutation(
            sharedContext.getCreateImportResponse().get("id").toString(),
            employeeId
        );
        var response = apiHelper.graphQlRequest(payload, "completeImport");
        log.debug("Complete import response: {}", response);
        if (response.get("data") != null) {
            sharedContext.setCompleteImportResponse((Map) response.get("data"));
        } else {
            sharedContext.setCompleteImportResponse(null);
        }

        return response;
    }

    public String getCompleteImportStatus(){
        String payload = GraphQLQueryMapper.getImportById(sharedContext.getLastImportId());

        var response = apiHelper.graphQlRequest(payload, "findImportById");
        log.debug("Complete Status Response :{}",response);
        var data = response.get("data");
        if (data != null) {
            return ((Map) data).get("status").toString();
        } else {
            return null;
        }



    }
}
