package com.arcone.biopro.distribution.receiving.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@Builder(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product implements Validatable {

    private final String id;
    private final String productCode;
    private final String shortDescription;
    private final String productFamily;
    private final boolean active;
    private final ZonedDateTime createDate;
    private final ZonedDateTime modificationDate;

    public Product(String id, String productCode, String shortDescription, String productFamily, boolean active, ZonedDateTime createDate, ZonedDateTime modificationDate) {
        this.id = id;
        this.productCode = productCode;
        this.shortDescription = shortDescription;
        this.productFamily = productFamily;
        this.active = active;
        this.createDate = createDate;
        this.modificationDate = modificationDate;

        checkValid();

    }

    @Override
    public void checkValid() {

        if (id == null) {
            throw new IllegalArgumentException("Id is required.");
        }

        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product Code is required.");
        }

        if (shortDescription == null || shortDescription.isBlank()) {
            throw new IllegalArgumentException("Short Description is required.");
        }

        if (productFamily == null || productFamily.isBlank()) {
            throw new IllegalArgumentException("Product Family is required.");
        }

        if (createDate == null) {
            throw new IllegalArgumentException("Create Date is required.");
        }

    }
}
