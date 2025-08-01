package com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.io.Serializable;

@Builder
public record ImportedBloodCenter(
    @Schema(
        title = "Blood Center Name",
        description = "Name of the blood center",
        example = "A-name",
        requiredMode = NOT_REQUIRED
    )
    String bloodCenterName,

    @Schema(
        title = "Address",
        description = "Address of the blood center",
        example = "An-address",
        requiredMode = NOT_REQUIRED
    )
    String address,

    @Schema(
        title = "Registration Number",
        description = "Registration number of the blood center",
        example = "284535752",
        requiredMode = NOT_REQUIRED
    )
    String registrationNumber,

    @Schema(
        title = "License Number",
        description = "License number of the blood center",
        requiredMode = NOT_REQUIRED
    )
    String licenseNumber
) implements Serializable {

}
