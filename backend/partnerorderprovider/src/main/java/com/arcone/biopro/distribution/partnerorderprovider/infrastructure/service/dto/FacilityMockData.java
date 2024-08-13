package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.service.dto;

import java.io.Serializable;
import java.util.List;

public record FacilityMockData(
    List<FacilityDTO> data
) implements Serializable {
}
