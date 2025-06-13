package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

public record RecoveredPlasmaCartonItemPackedOutputDTO(

    String unitNumber,
    String productCode,
    String packedByEmployeeId,
    ZonedDateTime packedDate,
    String status

) implements Serializable {
}
