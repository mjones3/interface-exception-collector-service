package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LookupEntity;
import lombok.*;

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

    public static Lookup fromRepository(LookupEntity entity) {
        var lookup = Lookup.builder()
            .id(entity.getId())
            .type(entity.getType())
            .optionValue(entity.getOptionValue())
            .descriptionKey(entity.getDescriptionKey())
            .orderNumber(entity.getOrderNumber())
            .active(entity.isActive())
            .build();

        lookup.checkValid();
        return lookup;
    }

    @Override
    public void checkValid() {
        if (this.id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (this.descriptionKey == null || this.descriptionKey.isBlank()) {
            throw new IllegalArgumentException("descriptionKey cannot be null or blank");
        }
    }

}
