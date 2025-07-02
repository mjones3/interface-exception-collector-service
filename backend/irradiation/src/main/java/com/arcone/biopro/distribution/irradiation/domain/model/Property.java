package com.arcone.biopro.distribution.irradiation.domain.model;

import lombok.*;

/**
 * Domain model representing an irradiation property.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"value"})
public class Property {
    private String key;
    private String value;
}
