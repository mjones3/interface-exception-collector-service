package com.arcone.biopro.distribution.irradiation.domain.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Volume {
    String type;
    Integer value;
    String unit;
}
