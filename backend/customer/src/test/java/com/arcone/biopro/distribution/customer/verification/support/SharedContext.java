package com.arcone.biopro.distribution.customer.verification.support;

import io.cucumber.spring.ScenarioScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Component
@NoArgsConstructor
@AllArgsConstructor
@ScenarioScope
public class SharedContext {

    private List<Map> apiMessageResponse;
    private Map<String, Object> apiErrorResponse;

    public void setApiMessageResponse(List<Map> notifications) {
        this.apiMessageResponse = notifications;
    }

    public void setApiErrorResponse(Map<String, Object> errorMap) {
        this.apiErrorResponse = errorMap;
    }
}
