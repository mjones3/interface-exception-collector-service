package com.arcone.biopro.distribution.order.verification.support;

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

    // Order data
    private Integer orderNumber;
    private String externalId;
    private Integer orderId;
    private String orderStatus;

    // General API response data
    private List<Map> apiMessageResponse;
}
