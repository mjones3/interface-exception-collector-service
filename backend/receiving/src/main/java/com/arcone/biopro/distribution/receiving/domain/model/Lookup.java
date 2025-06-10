package com.arcone.biopro.distribution.receiving.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Lookup implements Validatable {

    private final Long id;
    private final String type;
    private final String optionValue;
    private final String descriptionKey;
    private final int orderNumber;
    private final boolean active;

    public static Lookup fromRepository(Long id,String type,String optionValue,String descriptionKey,int orderNumber,boolean active) {
        var lookup = Lookup.builder()
            .id(id)
            .type(type)
            .optionValue(optionValue)
            .descriptionKey(descriptionKey)
            .orderNumber(orderNumber)
            .active(active)
            .build();

        lookup.checkValid();
        return lookup;
    }

    @Override
    public void checkValid() {
        if (this.id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type cannot be null or blank");
        }
        if (optionValue == null || optionValue.isBlank()) {
            throw new IllegalArgumentException("optionValue cannot be null or blank");
        }
        if (this.descriptionKey == null || this.descriptionKey.isBlank()) {
            throw new IllegalArgumentException("descriptionKey cannot be null or blank");
        }
    }

}
