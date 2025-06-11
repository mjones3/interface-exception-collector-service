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


    public Map enterShippingInformation(String temperatureCategory) {

        String payload = GraphQLQueryMapper.enterShippingInformation(temperatureCategory, employeeId, sharedContext.getLocationCode());
        return apiHelper.graphQlRequest(payload, "enterShippingInformation");
    }

    public boolean isTemperatureValid(String temperatureCategory, String temperatureValue) {
        String payload = GraphQLQueryMapper.validateTemperature(temperatureCategory, temperatureValue);
        var response = apiHelper.graphQlRequest(payload, "validateTemperature");

        return Boolean.parseBoolean(((Map) response.get("data")).get("valid").toString());
    }
}
