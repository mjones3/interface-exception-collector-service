package com.arcone.biopro.distribution.irradiation.adapter.irradiation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceValidationResult {
    private boolean valid;
    private String errorMessage;
}
