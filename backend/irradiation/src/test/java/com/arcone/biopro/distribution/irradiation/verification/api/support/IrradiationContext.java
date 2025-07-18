package com.arcone.biopro.distribution.irradiation.verification.api.support;

import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.graphql.ResponseError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Setter
@Getter
@ScenarioScope
@Component
public class IrradiationContext {

    private String location;

    private List<IrradiationInventoryOutput> inventoryList;

    private List<ResponseError> responseErrors;

}
