package com.arcone.biopro.distribution.shipping.verification.support;

import io.cucumber.spring.ScenarioScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
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

    // Order data
    private Long orderNumber;

    // Shipment data
    private Long shipmentId;
    private List<Map> shipmentItems;

    // Product data
    private String unitNumber;
    private String productCode;

    // ExternalTransfer Data
    private Long externalTransferId;

    // Packing data
    @Builder.Default
    private Integer totalPacked = 0;
    @Builder.Default
    private Integer totalVerified = 0;
    @Builder.Default
    private Integer totalRemoved = 0;
    @Builder.Default
    private Integer toBeRemoved = 0;

    // Verification data
    private Map cancelSecondVerificationResponse;

    // General API response data
    private List<Map> apiMessageResponse;

    private List<LinkedHashMap> apiMessageResultResponse;

    public void clear() {
        this.orderNumber = null;
        this.shipmentId = null;
        this.unitNumber = null;
        this.productCode = null;
        this.totalPacked = 0;
        this.totalVerified = 0;
        this.totalRemoved = 0;
        this.toBeRemoved = 0;
        this.cancelSecondVerificationResponse = null;
        this.apiMessageResponse = null;
        this.apiMessageResultResponse = null;
    }
}
