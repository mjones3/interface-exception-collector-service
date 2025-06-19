package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto;

import java.io.Serializable;

public record RecoveredPlasmaCartonItemUnpackedOutputDTO(

    String unitNumber,
    String productCode,
    String status

) implements Serializable {
}
