package com.arcone.biopro.distribution.irradiation.domain.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"value"})
public class Property {
    private String key;
    private String value;
}
