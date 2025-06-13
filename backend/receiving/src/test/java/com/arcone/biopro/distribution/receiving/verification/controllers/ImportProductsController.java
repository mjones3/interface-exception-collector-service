package com.arcone.biopro.distribution.receiving.verification.controllers;

import com.arcone.biopro.distribution.receiving.verification.support.ApiHelper;
import com.arcone.biopro.distribution.receiving.verification.support.SharedContext;
import com.arcone.biopro.distribution.receiving.verification.support.graphql.GraphQLQueryMapper;
import io.cucumber.spring.ScenarioScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
}
