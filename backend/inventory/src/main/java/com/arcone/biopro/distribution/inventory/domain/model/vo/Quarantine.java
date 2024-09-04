package com.arcone.biopro.distribution.inventory.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Quarantine {
    private Long externId;
    private String reason;
    private String comment;
}
