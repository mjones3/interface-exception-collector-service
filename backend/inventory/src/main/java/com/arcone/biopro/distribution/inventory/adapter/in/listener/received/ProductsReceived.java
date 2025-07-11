package com.arcone.biopro.distribution.inventory.adapter.in.listener.received;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "ProductsReceived",
    title = "ProductsReceived",
    description = "ProductsReceived"
)
public class ProductsReceived {
    private String locationCode;
    private String createEmployeeId;
    private Integer orderNumber;
    private String externalId;
    private String temperatureCategory;
    private Instant createDate;
    private BigDecimal temperature;
    private String temperatureUnit;
    private String transitTime;
    private String thermometerCode;
    private String comments;
    private List<ReceivedProduct> products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(
        name = "ReceivedProduct",
        title = "ReceivedProduct",
        description = "Received Product"
    )
    public static class ReceivedProduct {
        private String unitNumber;
        private String productCode;
        private Map<String, String> properties;
        private List<ReceivedConsequence> consequences;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(
        name = "ReceivedConsequence",
        title = "ReceivedConsequence",
        description = "Received Consequence"
    )
    public static class ReceivedConsequence {
        private String consequenceType;
        private List<String> consequenceReasons;
    }
}