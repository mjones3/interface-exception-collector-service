package com.arcone.biopro.distribution.receiving.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Builder
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BarcodeTranslation {

    Long id;

    String fromValue;

    String toValue;

    String sixthDigit;

}
