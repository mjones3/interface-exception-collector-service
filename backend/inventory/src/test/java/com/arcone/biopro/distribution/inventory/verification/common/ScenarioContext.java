package com.arcone.biopro.distribution.inventory.verification.common;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ScenarioScope // Use ScenarioScope for Cucumber tests
public class ScenarioContext {
    private String unitNumber;
    private String productCode;
}
