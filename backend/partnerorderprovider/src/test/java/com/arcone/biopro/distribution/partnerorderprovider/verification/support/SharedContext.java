package com.arcone.biopro.distribution.partnerorderprovider.verification.support;

import io.cucumber.spring.ScenarioScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

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
    private JSONObject apiMessageResponseBody;
    private int apiResponseCode;
    private HttpStatus apiResponseStatus;

    // Order data
    private String externalId;
}
