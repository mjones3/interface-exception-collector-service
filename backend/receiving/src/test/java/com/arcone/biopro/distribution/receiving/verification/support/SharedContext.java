package com.arcone.biopro.distribution.receiving.verification.support;

import io.cucumber.spring.ScenarioScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Component
@NoArgsConstructor
@AllArgsConstructor
@ScenarioScope
public class SharedContext {

    //Default data
    @Value("${default.ui.facility}")
    private String facility;

    @Value("${default.employee.id}")
    private String employeeId;

    @Value("${default.ui.facility}")
    private String locationCode;


    // General API response data
    private List<Map> apiListMessageResponse; // message from the response "notifications"
    private List<Map> apiShipmentListResponse; // list of shipments inside 'data' from the previous response
    private Map findShipmentApiResponse; // response of findById request
    private List<Map> apiMessageResponse;

    private Map apiErrorResponse;

    private Integer lastShipmentId;
    private Integer lastShipmentNumber;

    private Map shipmentCreateResponse;

    private List<Map> createCartonResponseList;
    private List<Map> packedProductsList;
    private List<Map> verifiedProductsList;
    private Map lastCartonResponse;
    private Map lastCloseCartonResponse;
    private Map lastRemoveCartonResponse;
    private Map linksResponse;

    // Shipment data
    private String initialShipmentDate;
    private String finalShipmentDate;
    private Map lastShipmentCloseResponse;


    // RecoveredPlasmaCriteriaConfiguration
    private String recoveredPlasmaCriteriaConfigurationCustomerCode;

    private Map lastCartonPackingSlipResponse;

    private Map lastUnacceptableUnitsReportResponse;

    private Map lastShippingSummaryReportResponse;
}
