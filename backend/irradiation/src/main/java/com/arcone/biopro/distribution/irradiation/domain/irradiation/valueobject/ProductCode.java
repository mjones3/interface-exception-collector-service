package com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject;

import lombok.Builder;
import org.springframework.util.Assert;

/**
 * Value object representing a product code in the irradiation domain.
 */
@Builder
public record ProductCode(String value) {

    public ProductCode {
        try {
            Assert.hasText(value, "Product code cannot be empty");
            Assert.isTrue(value.length() <= 10, "Product code cannot exceed 10 characters");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static ProductCode of(String value) {
        return new ProductCode(value);
    }
}
