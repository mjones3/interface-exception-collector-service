package com.arcone.biopro.distribution.irradiation.verification.api.support;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.CheckDigitResponseDTO;
import com.arcone.biopro.distribution.irradiation.application.dto.BatchProductDTO;
import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.verification.common.GraphQlResponse;
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

    private String deviceId;

    private List<IrradiationInventoryOutput> inventoryList;

    private List<ResponseError> responseErrors;

    private String batchId;

    private GraphQlResponse<?> graphQlResponse;

    private Map<String, Object> batchCompletionResult;

    private CheckDigitResponseDTO checkDigitResponse;

    private List<Map<String, String>> batchItems;

    private String quarantineNotification;

    private List<BatchProductDTO> batchProducts;

}
