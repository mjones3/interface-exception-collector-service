package com.arcone.biopro.distribution.inventory.adapter.in.listener.imported;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductsImportedMessage {
    private String locationCode;
    private String createEmployeeId;
    private String temperatureCategory;
    private Instant createDate;
    private Integer temperature;
    private String temperatureUnit;
    private Integer transitTime;
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
        private AboRhType aboRh;
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
