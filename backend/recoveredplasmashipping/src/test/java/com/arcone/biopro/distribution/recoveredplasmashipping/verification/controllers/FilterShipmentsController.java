package com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql.GraphQLQueryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FilterShipmentsController {
    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private TestUtils utils;

    @Autowired
    private SharedContext sharedContext;

    public void filterShipmentsByLocationList(String locationCodeList) {

        var response = apiHelper.graphQlRequest(GraphQLQueryMapper.searchShipment(
            utils.parseCommaSeparatedStringToGraphqlArrayList(locationCodeList),
            null,
            null,
            null,
            null,
            null,
            null,
            null
        ), "searchShipment");
        sharedContext.setApiListMessageResponse((List<Map>) (response.get("notifications")));
        if (response.get("data") != null) {
            var responseData = (List<Map>) ((Map) response.get("data")).get("content");
            sharedContext.setApiShipmentListResponse(responseData);
        } else {
            sharedContext.setApiShipmentListResponse(List.of());
        }
        log.info("Response: {}", response);
    }

    public void getAllShipmentsByLocationDateAndStatus(String locationCodeList, String dateFrom, String dateTo, String statuses) {

        var response = apiHelper.graphQlRequest(GraphQLQueryMapper.searchShipment(
            utils.parseCommaSeparatedStringToGraphqlArrayList(locationCodeList),
            null,
            utils.parseCommaSeparatedStringToGraphqlArrayList(statuses),
            null,
            null,
            "\"" + dateFrom + "\"",
            "\"" + dateTo + "\"",
            null
        ), "searchShipment");
        sharedContext.setApiListMessageResponse((List<Map>) (response.get("notifications")));
        if (response.get("data") != null) {
            var responseData = (List<Map>) ((Map) response.get("data")).get("content");
            sharedContext.setApiShipmentListResponse(responseData);
        } else {
            sharedContext.setApiShipmentListResponse(List.of());
        }
        log.info("Response: {}", response);
    }

    public void filterShipmentsByLocationListAndCustomAttribute(String locationCodeList, String attributeKey, String attributeValue) {
        locationCodeList = utils.parseCommaSeparatedStringToGraphqlArrayList(locationCodeList);

        var shipmentNumber = attributeKey.equals("shipmentNumber")
            ? "\"" + attributeValue + "\""
            : null;
        var shipmentStatusList = attributeKey.equals("shipmentStatusList")
            ? utils.parseCommaSeparatedStringToGraphqlArrayList(attributeValue)
            : null;
        var shipmentCustomerList = attributeKey.equals("shipmentCustomerList")
            ? utils.parseCommaSeparatedStringToGraphqlArrayList(attributeValue)
            : null;
        var productTypeList = attributeKey.equals("productTypeList")
            ? utils.parseCommaSeparatedStringToGraphqlArrayList(attributeValue)
            : null;
        var shipmentFrom = attributeKey.equals("shipmentDateRange")
            ? "\"" + utils.getCommaSeparatedList(attributeValue)[0] + "\""
            : null;
        var shipmentTo = attributeKey.equals("shipmentDateRange")
            ? "\"" + utils.getCommaSeparatedList(attributeValue)[1] + "\""
            : null;
        var transportationReferenceNumber = attributeKey.equals("transportationReferenceNumber")
            ? "\"" + attributeValue + "\""
            : null;

        // Add date range as default when no shipmentNumber is provided
        if (!List.of("shipmentNumber", "shipmentDateRange").contains(attributeKey)) {
            shipmentFrom = "\"" + LocalDate.now() + "\"";
            shipmentTo = "\"" + LocalDate.now().plusDays(1) + "\"";
        }

        var response = apiHelper.graphQlRequest(GraphQLQueryMapper.searchShipment(
            locationCodeList,
            shipmentNumber,
            shipmentStatusList,
            shipmentCustomerList,
            productTypeList,
            shipmentFrom,
            shipmentTo,
            transportationReferenceNumber
        ), "searchShipment");
        sharedContext.setApiListMessageResponse((List<Map>) (response.get("notifications")));

        if (response.get("data") != null) {
            var responseData = (List<Map>) ((Map) response.get("data")).get("content");
            sharedContext.setApiShipmentListResponse(responseData);
        } else {
            sharedContext.setApiShipmentListResponse(List.of());
        }
        log.info("Response: {}", response);
    }

}
