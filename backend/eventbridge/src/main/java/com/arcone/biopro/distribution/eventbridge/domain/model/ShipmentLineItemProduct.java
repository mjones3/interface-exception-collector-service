package com.arcone.biopro.distribution.eventbridge.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
public class ShipmentLineItemProduct {

    private final String unitNumber;
    private final String productCode;
    private final String bloodType;
    private final LocalDateTime expirationDate;
    private final ZonedDateTime collectionDate;
    private Map<String,String> attributes;

    public ShipmentLineItemProduct(String unitNumber,String productCode, String bloodType, LocalDateTime expirationDate, ZonedDateTime collectionDate) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.bloodType = bloodType;
        this.expirationDate = expirationDate;
        this.collectionDate = collectionDate;

        Assert.notNull(this.unitNumber, "unit number must not be null");
        Assert.notNull(this.productCode, "productCode number must not be null");
        Assert.notNull(this.bloodType, "bloodType number must not be null");
        Assert.notNull(this.expirationDate, "expirationDate number must not be null");
        Assert.notNull(this.collectionDate, "collectionDate number must not be null");
    }

    public void addAttribute(String key, String value) {
        Assert.notNull(key, "Key must not be null");
        Assert.notNull(value, "value must not be null");

        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }

}
