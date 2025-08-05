package com.arcone.biopro.distribution.receiving.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DevicePayload{
    private String id;
    private String serialNumber;
    private String name;
    private String device;
    private String deviceCategory;
    private String location;
    private String status;
    private ZonedDateTime createDate;
}
