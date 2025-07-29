package com.arcone.biopro.distribution.receiving.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentCompletedPayload {

    private Long orderNumber;
    private String externalOrderId;
    private String performedBy;
    private String locationCode;
    private String customerCode;
    private ZonedDateTime createDate;
    private String shipmentType;
    private String labelStatus;
    private String productCategory;
    private List<LineItem> lineItems;
    private Boolean quarantinedProducts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineItem {
        private String productFamily;
        private Integer quantity;
        private String bloodType;
        private List<Product> products;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Product {
        private String unitNumber;
        private String productFamily;
        private String productCode;
        private String productDescription;
    }
}
