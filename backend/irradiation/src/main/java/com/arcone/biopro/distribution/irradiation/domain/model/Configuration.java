package com.arcone.biopro.distribution.irradiation.domain.model;

import com.arcone.biopro.distribution.irradiation.domain.model.vo.ConfigurationKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuration implements Serializable {
    private ConfigurationKey key;
    private String value;
}
