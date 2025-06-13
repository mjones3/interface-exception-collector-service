package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.enumeration.ParseType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BarcodePattern {

    String pattern;

    Integer matchGroups;

    ParseType parseType;
}
