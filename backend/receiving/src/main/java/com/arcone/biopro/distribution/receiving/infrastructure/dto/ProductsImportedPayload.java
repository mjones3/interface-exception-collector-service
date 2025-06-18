package com.arcone.biopro.distribution.receiving.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductsImportedPayload {

    private String locationCode;
    private String createEmployeeId;
    private String temperatureCategory;
    private ZonedDateTime createDate;
    private BigDecimal temperature;
    private String temperatureUnit;
    private String transitTime;
    private String thermometerCode;
    private String comments;
    private List<ImportedProduct> products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportedProduct {
        private String unitNumber;
        private String productCode;
        private String productFamily;
        private String productDescription;
        private String aboRh;
        private LocalDateTime expirationDate;
        private Map<String, String> properties;
        private List<ImportedConsequence> consequences;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportedConsequence {
        private String consequenceType;
        private List<String> consequenceReasons;
    }
}
